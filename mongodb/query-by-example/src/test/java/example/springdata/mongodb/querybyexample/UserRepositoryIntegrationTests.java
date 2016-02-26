/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package example.springdata.mongodb.querybyexample;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.springframework.data.domain.ExampleSpec.GenericPropertyMatchers.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleSpec;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Integration test showing the usage of MongoDB Query-by-Example support through Spring Data repositories.
 *
 * @author Mark Paluch
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = ApplicationConfiguration.class)
public class UserRepositoryIntegrationTests {

	@Autowired UserRepository repository;

	User skyler, walter, flynn, marie, hank;

	@Before
	public void setUp() {

		repository.deleteAll();

		this.skyler = repository.save(new User("Skyler", "White", 45));
		this.walter = repository.save(new User("Walter", "White", 50));
		this.flynn = repository.save(new User("Walter Jr. (Flynn)", "White", 17));
		this.marie = repository.save(new User("Marie", "Schrader", 38));
		this.hank = repository.save(new User("Hank", "Schrader", 43));
	}

	/**
	 * @see DATAMONGO-1245
	 */
	@Test
	public void countBySimpleExample() {

		Example<User> example = Example.of(new User(null, "White", null));

		assertThat(repository.count(example), is(3L));
	}

	/**
	 * @see DATAMONGO-1245
	 */
	@Test
	public void ignorePropertiesAndMatchByAge() {

		Example<User> example = ExampleSpec.of(User.class). //
				withIgnorePaths("firstname", "lastname").//
				createExample(flynn);

		assertThat(repository.findOne(example), is(flynn));
	}

	/**
	 * @see DATAMONGO-1245
	 */
	@Test
	public void substringMatching() {

		Example<User> example = ExampleSpec.of(User.class).//
				withStringMatcherEnding()//
				.createExample(new User("er", null, null));

		assertThat(repository.findAll(example), hasItems(skyler, walter));
	}

	/**
	 * @see DATAMONGO-1245
	 */
	@Test
	public void regexMatching() {

		Example<User> example = ExampleSpec.of(User.class).//
				withMatcher("firstname", matcher -> matcher.regex()).//
				createExample(new User("(Skyl|Walt)er", null, null));

		assertThat(repository.findAll(example), hasItems(skyler, walter));
	}

	/**
	 * @see DATAMONGO-1245
	 */
	@Test
	public void matchStartingStringsIgnoreCase() {

		Example<User> example = ExampleSpec.of(User.class). //
				withIgnorePaths("age").//
				withMatcher("firstname", startsWith()).//
				withMatcher("lastname", ignoreCase()).//
				createExample(new User("Walter", "WHITE", null));

		assertThat(repository.findAll(example), hasItems(flynn, walter));
	}

	/**
	 * @see DATAMONGO-1245
	 */
	@Test
	public void configuringMatchersUsingLambdas() {

		Example<User> example = ExampleSpec.of(User.class).withIgnorePaths("age"). //
				withMatcher("firstname", matcher -> matcher.startsWith()). //
				withMatcher("lastname", matcher -> matcher.ignoreCase()). //
				createExample(new User("Walter", "WHITE", null));

		assertThat(repository.findAll(example), hasItems(flynn, walter));
	}

	/**
	 * @see DATAMONGO-1245
	 */
	@Test
	public void valueTransformer() {

		Example<User> example = ExampleSpec.of(User.class). //
				withMatcher("age", matcher -> matcher.transform(value -> Integer.valueOf(50))).//
				createExample(new User(null, "White", 99));

		assertThat(repository.findAll(example), hasItems(walter));
	}

}