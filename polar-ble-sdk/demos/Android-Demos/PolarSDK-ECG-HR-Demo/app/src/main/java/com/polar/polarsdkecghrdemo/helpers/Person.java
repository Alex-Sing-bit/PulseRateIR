package com.polar.polarsdkecghrdemo.helpers;

import java.util.ArrayList;

public class Person {

    public static final String[] phoneNumberPatterns = new String[]
            {"^\\+[7-8]-[0-9]{3}-[0-9]{3}-[0-9]{2}-[0-9]{2}$",
                    "^\\+[7-8][0-9]{3}[0-9]{3}[0-9]{2}[0-9]{2}$",
                    "^[7-8]-[0-9]{3}-[0-9]{3}-[0-9]{2}-[0-9]{2}$",
                    "^[7-8][0-9]{3}[0-9]{3}[0-9]{2}[0-9]{2}$"};
    private int id = -1;
    private String name = null;

    private Mood mood = Mood.CALM;
    private int pulseRate = 72;

    private int averageHr = 72;

    private String phoneNumber = null;
    private String hrId = null;
    private ArrayList<Integer> yHrValues = new ArrayList<>();
    private final int valueSize = 100;


    public Person() {
    }

    public Person(String number, String hrId, String name, Mood mood) {
        setId(number);
        setPhoneNumber(number);
        setHrId(hrId);
        this.name = name;
        this.mood = mood;
    }



    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getPulseRate() {
        return pulseRate;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public Mood getMood() {
        return mood;
    }

    public String getHrId() {
        return hrId;
    }

    public ArrayList<Integer> getyHrValues() {
        return yHrValues;
    }

    public void setMood(int pulseRate) {
        addValue(pulseRate);
        this.pulseRate = pulseRate;//+ new Random().nextInt(11) - 5;
        //averageHr = (averageHr + pulseRate) / 2;

        int lowerLimit = (int) (averageHr * 0.85);
        int upperLimit = (int) (averageHr * 1.15);

        if (pulseRate < lowerLimit) {
            System.out.println("Пульс " + this.pulseRate + " ниже нормы. Нижняя граница: " + lowerLimit);
            this.mood = Mood.TIRED;
        } else if (pulseRate > upperLimit) {
            System.out.println("Пульс " + this.pulseRate + " выше нормы. Верхняя граница: " + upperLimit);
            this.mood = Mood.STRESSED;
        } else {
            System.out.println("Пульс " + this.pulseRate + " в норме");
            this.mood = Mood.CALM;
        }

    }

    public void setId(String number) {
        if (isPhoneNumber(number)) {
            this.id = makeId(number);
        }
    }

    public static int makeId(String s) {
        return s.hashCode();
    }

    public void setName(String name) {
        this.name = name;
    }


    public void setHrId(String hrId) {
        if (isHrId(hrId))
            this.hrId = hrId;
    }

    public static boolean isHrId(String hrId) {
        return (hrId.matches("^[a-zA-Z0-9]{8}$"));
    }

    public void setPhoneNumber(String phoneNumber) {

        if (isPhoneNumber(phoneNumber)) {
            this.phoneNumber = makePN(phoneNumber);
        }
    }

    public static String makePN(String phoneNumber) {
        String num = "+";
        for (char c : phoneNumber.toCharArray()) {
            if (Character.isDigit(c)) {
                num += c;
            }
        }
        return num;
    }

    public static boolean isPhoneNumber(String barcode) {
        if (barcode == null) {
            return false;
        }
        for (String phoneNumberPattern : phoneNumberPatterns) {
            if (barcode.matches(phoneNumberPattern)) {
                return true;
            }
        }
        return false;
    }
    public void addValue(int pulseRate) {
        if (yHrValues.size() >= valueSize) {
            yHrValues.remove(0);
        }
        yHrValues.add(pulseRate);
    }
}
