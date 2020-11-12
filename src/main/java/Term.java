import java.util.ArrayList;
import java.util.List;

public class Term {
    private String description;
    private List<Job> jobs;
    private transient List<String> ids;

    public Term() {
        this.jobs = new ArrayList<>();
        this.ids = new ArrayList<>();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Job> getJobs() {
        return jobs;
    }

    public void setJobs(List<Job> jobs) {
        this.jobs = jobs;
    }

    public List<String> getIds() {
        return ids;
    }

    public void setIds(List<String> ids) {
        this.ids = ids;
    }
}
