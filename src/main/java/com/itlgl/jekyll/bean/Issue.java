package com.itlgl.jekyll.bean;

import java.util.List;

public class Issue {
    public int number;
    public String title;
    public List<Label> labels;
    public String created_at;
    public String updated_at;
    public String body;
    public String author_association;
}
