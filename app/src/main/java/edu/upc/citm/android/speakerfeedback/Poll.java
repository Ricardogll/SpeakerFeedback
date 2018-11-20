package edu.upc.citm.android.speakerfeedback;

import java.util.Date;
import java.util.List;

public class Poll {
    private String question;
    private List<String> options;
    private boolean open;
    private Date start, end;
    private List<Integer> result;


    Poll() {}

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public List<String> getOptions() {
        return options;
    }

    public void setOptions(List<String> options) {
        this.options = options;
    }

    public String getOptionsAsString(){

        StringBuilder b = new StringBuilder();
        for(String op : options){
            b.append(op);
            b.append("\n");
        }
        return b.toString();
    }
    public boolean isOpen() {
        return open;
    }

    public void setOpen(boolean open) {
        this.open = open;
    }

    public Date getStart() {
        return start;
    }

    public void setStart(Date start) {
        this.start = start;
    }

    public Date getEnd() {
        return end;
    }

    public void setEnd(Date end) {
        this.end = end;
    }

    public List<Integer> getResult() {
        return result;
    }

    public void setResult(List<Integer> result) {
        this.result = result;
    }
}
