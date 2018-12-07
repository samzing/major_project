package com.sss.services;


import com.sss.classModel.*;
import org.springframework.stereotype.Service;

import java.util.*;


public class TimeTableGenerator {


    public static ArrayList<FacultyResponse> teachersData;
    public static HashMap<String, Course> courseDataHM;
    public static ArrayList<FirstYearGroup> firstYearData;
    public static ArrayList<PhdGroup> phdData;
    public static ArrayList<Section> sections;


    public TimeTableGenerator(ArrayList<FacultyResponse> teachersData,
                              ArrayList<Course> courseDataList,
                              ArrayList<FirstYearGroup> firstYearData,
                              ArrayList<PhdGroup> phdData,
                              ArrayList<Section> sections) {
        this.teachersData = teachersData;
        this.courseDataHM = new HashMap<>();
        for (Course course : courseDataList) {
            this.courseDataHM.put(course.courseId, course);
        }
        this.firstYearData = firstYearData;
        this.phdData = phdData;

        this.sections = sections;

        System.out.println("gleba in first function 2 call at 0");

    }




    static void assignElectives(Section[] sections, Teacher[] teachers, Room[] rooms) {

        for (int section = 0; section < sections.length; section+=2) {
            for (int d = 0; d < 5; d++) {
                for (int h = 0; h < 10; h++) {
                    if (!sections[section].timeTable.timetable[d][h].equals("null")) {
                        String sub = sections[section].timeTable.timetable[d][h];
                        for (Teacher t : teachers) {
                            if (t.facultyResponse.subject1.equals(sub)) {
                                t.assign(d, h, (section / 2) + "-" + sub);
                                rooms[section / 2].assign(d, h, sub + "-" + t.facultyResponse.facultyid);
                            }
                            if (t.facultyResponse.subject2.equals(sub)) {
                                t.assign(d, h, (section / 2) + "-" + sub);
                                rooms[section / 2].assign(d, h, sub + "-" + t.facultyResponse.facultyid);
                            }
                            if (t.facultyResponse.subject3.equals(sub)) {
                                t.assign(d, h, (section / 2) + "-" + sub);
                                rooms[section / 2].assign(d, h, sub + "-" + t.facultyResponse.facultyid);
                            }

                        }
                    }
                }
            }
        }
    }


    static class sortTeachersByRemainingHours implements Comparator<Teacher> {
        public int compare(Teacher a, Teacher b) {
            return b.hoursToAssign - a.hoursToAssign;
        }
    }

    static void assignTeachersToFirstYear(Teacher[] teachers) {
        /*
         * 8 0 9 1 10 2 11 3 12 4 1 5 2 6 3 7 4 8 5 9
         */

        int n = teachers.length;

        PriorityQueue<Teacher> pq = new PriorityQueue<>(n, new sortTeachersByRemainingHours());
        for (Teacher t : teachers) {
            pq.add(t);
        }

        for (FirstYearGroup firstYear : firstYearData) {
            String[][] timetable = firstYear.timeTable.timetable;
            int i = 0;
            int[] days = new int[4];
            int[] hours = new int[4];

            for (int d = 0; d < 5; d++) {
                for (int h = 0; h < 10; h++) {
                    if (!timetable[d][h].equals("null")) {
                        days[i] = d;
                        hours[i] = h;
                    }
                }
            }
            DaysHours daysHours = new DaysHours(days, hours);
            if(i!=0)
            firstYearHelper(pq, daysHours, firstYear.groupId.charAt(0),
                    Integer.parseInt(firstYear.groupId.substring(1)));
        }


    }

    static void firstYearHelper(PriorityQueue<Teacher> pq,
                                DaysHours daysHours, char group, int i) {
        int[] days = daysHours.days;
        int[] hours = daysHours.hours;

        Teacher t = pq.poll();
        ArrayList<Teacher> al = new ArrayList<>();
        al.add(t);
        while (true) {
            if ((t.isFree(days[0], hours[0]) && t.isFree(days[1], hours[1])) &&
                    (t.isFree(days[2], hours[2]) && t.isFree(days[3], hours[3]))
                    && t.current1Batches < t.total1Batches) {
                break;
            }
            t = pq.poll();
            al.add(t);
        }
        t.assign(days[0], hours[0], "" + group + (i + 1));
        t.assign(days[1], hours[1], "" + group + (i + 1));
        t.assign(days[2], hours[2], "" + group + (i + 1));
        t.assign(days[3], hours[3], "" + group + (i + 1));
        t.current1Batches++;
        for (Teacher teacher : al) {
            pq.add(teacher);
        }
    }

    static class DaysHours {
        int[] days;
        int[] hours;

        DaysHours(int[] days, int[] hours) {
            this.days = days;
            this.hours = hours;
        }
    }




    static void assignDecLabs(Teacher[] teachers, Room lab, Section[] sections) {
        Random rand = new Random();
        int N = teachers.length;
        for (int section = 0; section < sections.length; section++) {
            for (int d = 0; d < 5; d++) {
                for (int h = 0; h < 10; h++) {
                    if (!sections[section].timeTable.timetable[d][h].equals("null")
                            && sections[section].timeTable.timetable[d][h].substring(9).equals("lab")) {
                        while (true) {
                            int n = rand.nextInt(N);

                            if (teachers[n].isFree(d, h) && teachers[n].isFree(d, h + 1) &&
                                    sections[section].isFree(d, h) && sections[section].isFree(d, h + 1)) {
                                teachers[n].assign(d, h, sections[section].timeTable.timetable[d][h]);
                                teachers[n].assign(d, h + 1, sections[section].timeTable.timetable[d][h]);
                                lab.assign(d, h, sections[section].timeTable.timetable[d][h] + "-" + teachers[n].facultyResponse.facultyid);
                                lab.assign(d, h + 1, sections[section].timeTable.timetable[d][h] + "-" + teachers[n].facultyResponse.facultyid);
                                //h++;
                                break;
                            }
                        }
                        while (true) {
                            int n = rand.nextInt(N);

                            if (teachers[n].isFree(d, h) && teachers[n].isFree(d, h + 1)) {
                                teachers[n].assign(d, h, sections[section].timeTable.timetable[d][h]);
                                teachers[n].assign(d, h + 1, sections[section].timeTable.timetable[d][h]);
                                lab.assignSecondTeacher(d, h, teachers[n].facultyResponse.facultyid);
                                lab.assignSecondTeacher(d, h + 1, teachers[n].facultyResponse.facultyid);
                                h++;
                                break;
                            }
                        }
                    }
                }
            }
        }
    }


    static void assignLabs(Teacher[] teachers, Room lab, Section[] sections) {

        HashMap<String, Integer> coursesWithLab = new HashMap<>();

        for (Teacher t : teachers) {

            String sub = t.facultyResponse.subject1;
            if (sub.length() == 5 && courseDataHM.get(sub).containsLab) {
                coursesWithLab.put(sub, 1);
            }
            sub = t.facultyResponse.subject2;
            if (sub.length() == 5 && courseDataHM.get(sub).containsLab) {
                coursesWithLab.put(sub, 1);
            }
            sub = t.facultyResponse.subject3;
            if (sub.length() == 5 && courseDataHM.get(sub).containsLab) {
                coursesWithLab.put(sub, 1);
            }

        }
        Random rand = new Random();
        int N = teachers.length;
        for (String sub : coursesWithLab.keySet()) {
            int d, h;
            while (true) {
                d = rand.nextInt(5);
                h = rand.nextInt(9);
                int n = rand.nextInt(N );
                int section = getSection(sub + "lab", sections);
                if (teachers[n].isFree(d, h) && teachers[n].isFree(d, h + 1) &&
                        lab.isFree(d, h) && lab.isFree(d, h + 1) &&
                        sections[section].isFree(d, h) && sections[section].isFree(d, h + 1)) {
                    sections[section].subjects.put(sub + "lab", 1);
                    teachers[n].assign(d, h, "lab-" + section + "-" + sub + " lab");
                    teachers[n].assign(d, h + 1, "lab-" + section + "-" + sub + " lab");
                    lab.assign(d, h, section + "-" + sub + "-" + teachers[n].facultyResponse.facultyid);
                    lab.assign(d, h + 1, section + "-" + sub + "-" + teachers[n].facultyResponse.facultyid);
                    sections[section].assign(d, h + 1,
                            "lab-" + section + "-" + sub);
                    sections[section].assign(d, h + 1,
                            "lab-" + section + "-" + sub);
                    break;
                }
            }
            while (true) {
                int n = rand.nextInt(N);
                int section = getSection(sub + "lab", sections);

                if (teachers[n].isFree(d, h) && teachers[n].isFree(d, h + 1)) {
                    teachers[n].assign(d, h, sections[section].timeTable.timetable[d][h]);
                    teachers[n].assign(d, h + 1, sections[section].timeTable.timetable[d][h]);
                    lab.assignSecondTeacher(d, h, teachers[n].facultyResponse.facultyid);
                    lab.assignSecondTeacher(d, h + 1, teachers[n].facultyResponse.facultyid);

                    break;
                }
            }
        }

    }





    static void assignPhdLectures(Teacher[] teachers, Room[] rooms, Section[] sections){
        for(PhdGroup phdGroup : phdData){
            String[][] timetable = phdGroup.timeTable.timetable;

            for(int d=0;d<5;d++){
                for(int h=0;h<10;h++){
                    if(!timetable[d][h].equals("null")){
                        String sub = timetable[d][h];
                        for(Teacher t : teachers){
                            if(t.facultyResponse.subject1.equals(sub)){
                                t.assign(d, h, sub);
                            }
                            if(t.facultyResponse.subject2.equals(sub)){
                                t.assign(d, h, sub);
                            }
                            if(t.facultyResponse.subject3.equals(sub)){
                                t.assign(d, h, sub);
                            }
                        }
                    }
                }
            }
        }
    }





    static void assignDccCourses(Teacher[] teachers, Room[] rooms, Section[] sections) {

        // TODO change for loop by priority queue
        for (Teacher t : teachers) {

            String sub;
            for (int sub_n = 1; sub_n <= 3; sub_n++) {
                if (sub_n == 1) {
                    sub = t.facultyResponse.subject1;
                } else if (sub_n == 2) {
                    sub = t.facultyResponse.subject2;
                } else {
                    sub = t.facultyResponse.subject3;
                }

                if (sub.equals("null")) {
                    continue;
                }

                int section = getSection(sub, sections);
                int room = section / 2;

                int subjectHours = courseDataHM.get(sub).tutHours;
                for (int hour = 1; hour <= subjectHours; hour++) {
                    boolean assigned = false;

                    for (int i = 0; i < 5; i++) {
                        for (int j = 0; j < 10; j++) {
                            if (!t.days[i] && !t.slots[j] && sections[section].isFree(i, j)
                                    && rooms[room].isFree(i, j)) {
                                t.assign(i, j, room + "-"+section+"-"+sub);
                                rooms[room].assign(i, j, section+"-"+sub+"-"+t.facultyResponse.facultyid);
                                sections[section].assign(i, j, room+"-"+sub+"-"+t.facultyResponse.facultyid);
                                assigned = true;
                                break;
                            }
                        }
                        if (assigned)
                            break;
                    }
                    if (assigned)
                        continue;

                    for (int i = 0; i < 5; i++) {
                        if (!t.days[i]) {
                            for (int j = 0; j < 10; j++) {
                                if (t.isFree(i, j) && sections[section].isFree(i, j)
                                        && rooms[room].isFree(i, j)) {
                                    t.assign(i, j, room + "-"+section+"-"+sub);
                                    rooms[room].assign(i, j, section+"-"+sub+"-"+t.facultyResponse.facultyid);
                                    sections[section].assign(i, j, room+"-"+sub+"-"+t.facultyResponse.facultyid);
                                    assigned = true;
                                    break;
                                }
                            }
                        }

                        if (assigned)
                            break;
                    }
                    if (assigned)
                        continue;

                    for (int j = 0; j < 10; j++) {
                        if (!t.slots[j]) {
                            for (int i = 0; i < 5; i++) {
                                if (t.isFree(i, j) && sections[section].isFree(i, j)
                                        && rooms[room].isFree(i, j)) {
                                    t.assign(i, j, room + "-"+section+"-"+sub);
                                    rooms[room].assign(i, j, section+"-"+sub+"-"+t.facultyResponse.facultyid);
                                    sections[section].assign(i, j, room+"-"+sub+"-"+t.facultyResponse.facultyid);
                                    assigned = true;
                                    break;
                                }
                            }
                        }

                        if (assigned)
                            break;
                    }
                    if (assigned)
                        continue;
                    for (int i = 0; i < 5; i++) {
                        for (int j = 0; j < 10; j++) {
                            if (t.isFree(i, j) && sections[section].isFree(i, j)
                                    && rooms[room].isFree(i, j)) {
                                t.assign(i, j, room + "-"+section+"-"+sub);
                                rooms[room].assign(i, j, section+"-"+sub+"-"+t.facultyResponse.facultyid);
                                sections[section].assign(i, j, room+"-"+sub+"-"+t.facultyResponse.facultyid);
                                assigned = true;
                                break;
                            }
                        }
                        if (assigned)
                            break;
                    }

                }

            }
        }
    }

    static int getSection(String sub, Section[] sections) {
        int year = Integer.parseInt(sub.charAt(2) + "");
        int section;
        if (year == 2) {
            section = 0;
        } else if (year == 3) {
            section = 2;
        } else {
            section = 3;
        }
        if (sections[section].subjects.containsKey(sub)) {
            section++;
        }
        return section;
    }




    public OverallTT generateTimeTable(){

        System.out.println("gleba in first function 2 call at 1");

        Room[] rooms = new Room[4];
        for(int i=0;i<4;i++){
            rooms[i]=new Room(i);
        }
        Teacher[] teachers= new Teacher[teachersData.size()];
        for(int i=0;i<teachersData.size();i++){
            teachers[i]=new Teacher(teachersData.get(i));
        }

        System.out.println("gleba in first function 2 call at 2");

        Section[] sectionsArray = new Section[sections.size()];
        for(int i=0;i<sections.size();i++){
            sectionsArray[i]=sections.get(i);
        }

        System.out.println("gleba in first function 2 call at 3");

        assignElectives(sectionsArray, teachers, rooms);

        System.out.println("gleba in first function 2 call at 4");


        assignPhdLectures(teachers,rooms,sectionsArray);

        System.out.println("gleba in first function 2 call at 5");

        assignDecLabs(teachers, rooms[3], sectionsArray);

        System.out.println("gleba in first function 2 call at 6");

        assignTeachersToFirstYear(teachers);

        System.out.println("gleba in first function 2 call at 7");

        assignLabs(teachers, rooms[3], sectionsArray);

        System.out.println("gleba in first function 2 call at 8");
        assignDccCourses(teachers, rooms, sectionsArray);

        System.out.println("gleba in first function 2 call at 9");
        OverallTT overallTT = new OverallTT();

        for(Teacher t : teachers){
            overallTT.facultyResponses.add(t.facultyResponse);
        }

        System.out.println("gleba in first function 2 call at 10");
        overallTT.sections=sections;
        for(Room room : rooms){
            overallTT.rooms.add(room);
        }

        System.out.println("gleba in first function 2 call at 11");

        return overallTT;
    }
}