package com.nouroeddinne.chatapp;

public class Model {

    String from;
    String msg;

    public Model(String from, String msg) {
        this.from = from;
        this.msg = msg;
    }

    public Model() {
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
