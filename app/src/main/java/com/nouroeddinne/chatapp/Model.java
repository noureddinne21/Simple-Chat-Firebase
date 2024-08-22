package com.nouroeddinne.chatapp;

public class Model {

    String from;
    String msg;
    String Name;

    public Model(String from, String msg, String name) {
        this.from = from;
        this.msg = msg;
        this.Name = name;
    }

    public Model(String from, String msg) {
        this.from = from;
        this.msg = msg;
    }

    public Model() {
    }

    public String getName() {
        return Name;
    }

    public void setName(String Name) {
        this.Name = Name;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
