import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class Main {
    private static final List<String> allIds = new ArrayList<>();

    public static void main(String args[]) {
        try {
            System.setProperty("webdriver.chrome.driver", "C:\\Desenvolvimento\\ChromeWebDriver\\chromedriver.exe");
            WebDriver driver = new ChromeDriver();
            //System.setProperty("webdriver.gecko.driver", "C:\\Desenvolvimento\\ChromeWebDriver\\geckodriver.exe");
            //WebDriver driver = new FirefoxDriver();

            //read cities from cities.txt
            List<City> cities = new ArrayList<>();
            BufferedReader reader = new BufferedReader(new FileReader("cities.txt"));
            String line = reader.readLine();
            while (line != null) {
                City city = new City();
                city.setName(line);
                city.setParsedName(line.replaceAll(" ", "+").replaceAll(",", "%2C+"));
                cities.add(city);

                line = reader.readLine();
            }
            reader.close();

            //read terms from terms.txt
            List<String> termsList = new ArrayList<>();
            BufferedReader readerTerms = new BufferedReader(new FileReader("terms.txt"));
            line = readerTerms.readLine();
            while (line != null) {
                termsList.add(line);
                line = readerTerms.readLine();
            }
            readerTerms.close();

            for (City city : cities) {
                Integer count = 0;
                for (String description : termsList) {
                    Term term = new Term();
                    term.setDescription(description);
                    term.setIds(getIds(driver, description, city.getParsedName(), term.getIds()));
                    term.setJobs(getJobs(driver, term.getIds()));
                    city.getTerms().add(term);

                    count += term.getJobs().size();
                }
                city.setCount(count);
            }

            cities = cities.stream().filter(city -> city.getCount() > 0).collect(Collectors.toList());

            Output output = new Output();
            output.setCities(cities);
            Integer sum = 0;
            for (int i = 0; i < cities.size(); i++) {
                sum += cities.get(i).getCount();
            }
            output.setCount(sum);

            try (Writer writer = new FileWriter("jobs.json")) {
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                gson.toJson(output, writer);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static List<String> getIds(WebDriver driver, String description, String location, List<String> ids) {
        System.out.println("getIds " + description + location + "  started " + new Date());

        //driver.get("https://www.indeed.com.br/jobs?q=" + description + "&l=" + location + "&filter=0");
        driver.get("https://www.indeed.com.br/empregos?q=" + description + "&l=" + location);

        int count = 990;
        try {
            WebElement webElement = driver.findElement(By.id("searchCountPages"));
            if (webElement != null) {
                count = Integer.parseInt(webElement.getText().split("de")[1].replace("vagas", "").trim()) + 10;
            }
        } catch (Exception e) {
            System.out.println("getIds not found " + description + location + " " + new Date());
            return new ArrayList<>();
        }

        for (int i = 0; i <= count; i = i + 10) {
            String url = "https://www.indeed.com.br/jobs?q=" + description + "&l=" + location + "&start=" + i;
            try {
                driver.get(url);
                List<WebElement> elements = driver.findElements(By.cssSelector("div[data-jk]"));
                for (WebElement element : elements) {
                    String id = element.getAttribute("data-jk");
                    if (!allIds.contains(id)) {
                        ids.add(id);
                        allIds.add(id);
                    }
                }
            } catch (Exception e) {
                System.out.println(url);
                e.printStackTrace();
            }
        }

        System.out.println("getIds " + description + location + " finished " + new Date());

        return ids;
    }

    private static List<Job> getJobs(WebDriver driver, List<String> ids) throws IOException {
        System.out.println("getJobs started " + new Date());

        List<Job> jobs = new ArrayList<>();

        for (String id : ids) {
            Job job = getJob(driver, id);
            if (job != null) {
                if (jobs.stream().noneMatch(j -> j.getTitle().equals(job.getTitle())
                        && j.getDescription().equals(job.getDescription()))) {
                    jobs.add(job);
                }
            }
        }

        System.out.println("getJobs finished " + new Date());

        return jobs;
    }

    private static Job getJob(WebDriver driver, String id) {
        try {
            //driver.get("https://www.indeed.com/viewjob?jk=" + id);
            driver.get("https://www.indeed.com.br/ver-emprego?jk=" + id);
            WebElement title = driver.findElement(By.cssSelector(".jobsearch-JobInfoHeader-title"));
            WebElement description = driver.findElement(By.cssSelector(".jobsearch-jobDescriptionText"));

            WebElement divLocation = driver.findElement(By.cssSelector(".jobsearch-DesktopStickyContainer-companyrating"));
            List<WebElement> divLocationElements = divLocation.findElements(By.tagName("div"));
            WebElement location = divLocationElements.get(divLocationElements.size() - 1);

            Job job = new Job();
            job.setId(id);
            job.setTitle(title.getText());
            job.setDescription(description.getText());
            job.setLocation(location.getText());

            return job;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
