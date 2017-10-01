/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uts.wsd;
import java.util.*;
import java.io.Serializable;
import javax.xml.bind.annotation.*;
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "students")

/**
 *
 * @author sawicky
 */
public class Students implements Serializable{
 @XmlElement(name = "student")
    private ArrayList<Student> list = new ArrayList<Student>();
 
    public ArrayList<Student> getList() {
        return list;
    }
    public void addStudent(Student student) {
        list.add(student);
    }
    public void removeStudent(Student student) {
        list.remove(student);
    }
    public Student login(String email, String password) {
        // For each user in the list...
        for (Student student : list) {
            if (student.getEmail().equals(email) && student.getPassword().equals(password))
                return student; // Login correct. Return this user.
        }
        return null; // Login incorrect. Return null.
    }   
}
