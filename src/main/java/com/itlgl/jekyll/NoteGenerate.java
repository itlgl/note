package com.itlgl.jekyll;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.itlgl.jekyll.bean.Issue;
import com.itlgl.jekyll.bean.Label;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.Charset;
import java.util.List;

public class NoteGenerate {

    static OkHttpClient client = new OkHttpClient();
    static Gson gson = new Gson();
    static File postsDir = new File("./_posts/");
    static String FILE_SEPARATOR = "\r\n";

    public static void main(String[] args) {
        System.out.println(postsDir.getAbsolutePath());
        System.out.println(postsDir.exists());
        String token = args != null && args.length > 0 ? args[0] : null;
        if(token == null || token.length() == 0) {
            System.out.println("get token fail");
            return;
        }
        try {
            run(token);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void run(String token) throws Exception {
        String url = "https://api.github.com/repos/itlgl/note/issues?per_page=1000&page=1";
        Request request = new Request.Builder()
                .url(url)
                .header("Authorization", token)
                .build();
        Response response = client.newCall(request).execute();
        String issueJson = response.body().string();

        File[] files = postsDir.listFiles();
        if(files != null) {
            for (File file : files) {
                file.delete();
                System.out.println("delete file " + file.getName());
            }
        }

        List<Issue> issueList = gson.fromJson(issueJson, new TypeToken<List<Issue>>() {
        }.getType());
        System.out.println("issue count=" + issueList.size());
        for (Issue issue : issueList) {
            if(!"OWNER".equals(issue.author_association)) {
                continue;
            }

            String number = String.valueOf(issue.number);
            String fileName = issue.created_at.substring(0, 10) + "-issues-" + number + ".md";
            System.out.println("write file " + fileName);

            StringBuilder content = new StringBuilder();
            content.append("---").append(FILE_SEPARATOR);
            content.append("title: ").append(issue.title).append(FILE_SEPARATOR);
            content.append("date: ").append(issue.created_at.replace("T", " ").replace("Z", " ")).append(FILE_SEPARATOR);
            content.append("categories: ").append(FILE_SEPARATOR).append("- note").append(FILE_SEPARATOR);
            content.append("tags: ").append(FILE_SEPARATOR);
            if(issue.labels != null && issue.labels.size() > 0) {
                for (Label label : issue.labels) {
                    content.append("- ").append(label.name).append(FILE_SEPARATOR);
                }
            }
            content.append("---").append(FILE_SEPARATOR);
            String issueBody = issue.body;
            // jekyll markdown末尾加上两个空格再换行视为换行
            issueBody = issueBody.replace("\r\n", "  \r\n");
            content.append(issueBody);

            File issueFile = new File(postsDir, fileName);
            FileOutputStream fos = new FileOutputStream(issueFile);
            IOUtils.write(content, fos, Charset.forName("utf-8"));
            fos.flush();
            fos.close();
            System.out.println("write file " + fileName + " SUCCESS");
        }
    }
}
