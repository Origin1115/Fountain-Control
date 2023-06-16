package com.origin.fountain.activity;

public class Sequence {
    private String seq_name;
    private int seq_id;
    private String seq_speed;



    public Sequence(int i, String name, String speed) {
        this.seq_id = i;
        this.seq_name = name;
        this.seq_speed = speed;
        return;
    }

    public String getSeq_name() {
        return seq_name;
    }

    public void setSeq_name(String seq_name) {
        this.seq_name = seq_name;
    }

    public int getSeq_id() {
        return seq_id;
    }

    public void setSeq_id(int seq_id) {
        this.seq_id = seq_id;
    }
    public String getSeq_speed() {
        return seq_speed;
    }

    public void setSeq_speed(String seq_speed) {
        this.seq_speed = seq_speed;
    }

}
