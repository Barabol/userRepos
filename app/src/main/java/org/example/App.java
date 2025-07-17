package org.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.web.bind.annotation.PathVariable;

@SpringBootApplication
@RestController
public class App {

	public static void main(String[] args) {
		SpringApplication.run(App.class, args);
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	private static class Repo {
		public String name;
		public boolean fork;
		public Owner owner;

		@JsonIgnoreProperties(ignoreUnknown = true)
		private static class Owner {
			public String login;

			@Override
			public String toString() {
				return login;
			}
		}

		@Override
		public String toString() {
			return "\"name\": \"" + name + "\", \"owner\": \"" + owner.toString() + "\"";
		}
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	private static class Branch {
		public String name;
		public Commit commit;

		@JsonIgnoreProperties(ignoreUnknown = true)
		private static class Commit {
			public String sha;
		}

		@Override
		public String toString() {
			return "\"name\": \"" + name + "\", \"last_commit_sha\": \"" + commit.sha + "\"";
		}
	}

	private Branch[] getBranches(Repo repo) {
		// teoretycznie ta cała metoda może zostać przeniesiona do klasy Repo
		// ale rujnowało by to częściowo konsystencje
		RestTemplate template = new RestTemplate();
		ResponseEntity<Branch[]> response;
		response = template.getForEntity(
				"https://api.github.com/repos/" + repo.owner + "/" + repo.name + "/branches",
				Branch[].class);
		return response.getBody();
	}

	private Repo[] getRepos(String userName) {
		RestTemplate template = new RestTemplate();
		ResponseEntity<Repo[]> response;
		response = template.getForEntity(
				"https://api.github.com/users/" + userName + "/repos",
				Repo[].class);
		return response.getBody();
	}

	private String parseToJson(Repo repo, Branch[] branches) {
		String ret = "{" + repo.toString() + ",\"branches\": [";
		for (int x = 0; x < branches.length; x++) {
			ret += "{" + branches[x].toString() + "}";
			if (x != branches.length - 1)
				ret += ",";
		}
		ret += "]}";
		return ret;
	}

	@GetMapping("/{userName}")
	public ResponseEntity<String> getUserInfo(@PathVariable String userName) {
		Repo[] repos;
		try {
			repos = getRepos(userName);
		} catch (HttpClientErrorException e) {
			return ResponseEntity.status(404).contentType(MediaType.APPLICATION_JSON)
					.body("{\"status\":" + e.getRawStatusCode() + ",\"message\":\"" + e.getStatusText() + "\"}");
		}

		Branch[] branches;
		String res = "[";

		for (int x = 0; x < repos.length; x++) {
			if (repos[x].fork)
				continue;
			try {
				branches = getBranches(repos[x]);
			} catch (HttpClientErrorException e) {
				return ResponseEntity.status(404).contentType(MediaType.APPLICATION_JSON)
						.body("{\"status\":" + e.getRawStatusCode() + ",\"message\":\"" + e.getStatusText() + "\"}");
			}
			res += parseToJson(repos[x], branches);
			if (x != repos.length - 1)
				res += ",";
		}
		res += "]";
		return ResponseEntity.status(200).contentType(MediaType.APPLICATION_JSON).body(res);
	}
}
