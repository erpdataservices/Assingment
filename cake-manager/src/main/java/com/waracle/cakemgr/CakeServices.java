package com.waracle.cakemgr;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

@Controller
@RequestMapping("/cakes")
public class CakeServices {

	public static final String URL_REGEX = "^(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";
	public static final String CAKE_DETAILS_ADDED_SUCCESSFULLY = "Cake details added successfully";
	public static final String INVALID_TITLE = "Invalid title";
	public static final String INVALID_DESCRIPTION = "Invalid description";
	public static final String TITLE_ALREADY_EXISTS = "Title Already Exists";
	public static final String INVALID_IMAGE_URL_PROVIDED = "Invalid image URL provided";
	@Autowired
	private CakeRepository cakeRepository;

	@GetMapping
	public ResponseEntity<Object> load() {

		System.out.println("downloading cake json");
		try (InputStream inputStream = new URL(
				"https://gist.githubusercontent.com/hart88/198f29ec5114a3ec3460/raw/8dd19a88f9b8d24c23d9960f3300d0c917a4f07c/cake.json")
						.openStream()) {
			BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

			StringBuffer buffer = new StringBuffer();
			String line = reader.readLine();
			while (line != null) {
				buffer.append(line);
				line = reader.readLine();
			}

			System.out.println("parsing cake json" + buffer.toString());

			JsonParser parser = new JsonFactory().createParser(buffer.toString());
			if (JsonToken.START_ARRAY != parser.nextToken()) {
				throw new Exception("bad token");
			}

			JsonToken nextToken = parser.nextToken();
			while (nextToken == JsonToken.START_OBJECT) {
				System.out.println("creating cake entity");

				CakeEntity cakeEntity = new CakeEntity();
				System.out.println(parser.nextFieldName());
				cakeEntity.setTitle(parser.nextTextValue());

				System.out.println(parser.nextFieldName());
				cakeEntity.setDescription(parser.nextTextValue());

				System.out.println(parser.nextFieldName());
				cakeEntity.setImage(parser.nextTextValue());

				if (cakeRepository.findByTitle(cakeEntity.getTitle()) == null) {
					cakeRepository.save(cakeEntity);
					System.out.println("adding cake entity");
				}

				nextToken = parser.nextToken();
				System.out.println(nextToken);

				nextToken = parser.nextToken();
				System.out.println(nextToken);
			}

		} catch (Exception ex) {
			System.out.println("Exception in loading cakes:");
			ex.printStackTrace();
		}

		return new ResponseEntity<Object>(cakeRepository.findAll(), HttpStatus.OK);

	}

	@PostMapping
	public ResponseEntity<Object> add(@RequestBody CakeEntity cakeEntity) {
		if (cakeEntity.getImage() == null || !cakeEntity.getImage().matches(URL_REGEX)) {
			return new ResponseEntity<Object>(INVALID_IMAGE_URL_PROVIDED, HttpStatus.BAD_REQUEST);
		} else if (StringUtils.isEmpty(cakeEntity.getDescription())) {
			return new ResponseEntity<Object>(INVALID_DESCRIPTION, HttpStatus.BAD_REQUEST);
		} else if (StringUtils.isEmpty(cakeEntity.getTitle())) {
			return new ResponseEntity<Object>(INVALID_TITLE, HttpStatus.BAD_REQUEST);
		} else if (cakeRepository.findByTitle(cakeEntity.getTitle()) == null) {
			cakeRepository.save(cakeEntity);
			System.out.println("adding cake entity");
			return new ResponseEntity<Object>(CAKE_DETAILS_ADDED_SUCCESSFULLY, HttpStatus.OK);
		} else {
			return new ResponseEntity<Object>(TITLE_ALREADY_EXISTS, HttpStatus.BAD_REQUEST);
		}

	}

}
