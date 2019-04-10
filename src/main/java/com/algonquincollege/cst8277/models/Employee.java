/**
 * File: Employee.java
 * Course materials (19W) CST 8277
 * (Students) @author: Can Shi 040806036 Zeyang Hu 040885680
 * (Modified) @date: 2019 03 13
 * (Professor) @author Mike Norman
 *
 * Copyright (c) 1998, 2009 Oracle. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 and Eclipse Distribution License v. 1.0
 * which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * Original @authors dclarke, mbraeuer
 *
 */
package com.algonquincollege.cst8277.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;

import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

/**
 * The Employee class demonstrates several JPA features:
 * <ul>
 * <li>Generated Id
 * <li>Version locking
 * <li>OneToOne relationship
 * <li>OneToMany relationship
 * <li>ManyToMany relationship
 * </ul>
 */
@Entity
@Table(name="EMPLOYEE")

public class Employee extends ModelBase implements Serializable {
    /** explicit set serialVersionUID */
    private static final long serialVersionUID = 1L;
    
    /**
     * first name of employee saved as string
     */
    protected String firstName;
    /**
     * last name of employee saved as string
     */
    protected String lastName;
    /**
     * salary of employee saved as a double
     */
    protected double salary;   
    /**
     * Address of the employee
     */
    protected Address address;
    
    /**
     * phone numbers of the employee
     */
    protected List<Phone> phones = new ArrayList<>();    
    /**
     * projects this employee is working on
     */
    protected List<Project> projects;

    // JPA requires each @Entity class have a default constructor
    public Employee() {
        super();
    }

    public Employee(String firstName, String lastName, double salary) {
        super();
        this.setFirstName(firstName);
        this.setLastName(lastName);
        this.setSalary(salary);
    }
    
    // Strictly speaking, JPA does not require hashcode() and equals(),
    // but it is a good idea to have one that tests using the PK (@Id) field
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + id;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Employee)) {
            return false;
        }
        Employee other = (Employee)obj;
        if (id != other.id) {
            return false;
        }
        return true;
    }
     
    /**
     * First Name getter
     * 
     * @return firstName the first name of employee
     */
    @Column(name="FIRSTNAME")
    public String getFirstName() {
        return firstName;
    }
    
    /**
     * 
     * @param firstName set First Name
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    
    /**
     * Last Name getter
     * 
     * @return lastName the last name of employee
     */
    @Column(name="LASTNAME")
    public String getLastName() {
        return lastName;
    }

    /**
     * 
     * @param lastName set Last Name
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * Salary getter
     * 
     * @return salary the salary of employee
     */
    @Column(name="SALARY")
    public double getSalary() {
        return salary;
    }

    /**
     * 
     * @param salary set Salary of employee
     */
    public void setSalary(double salary) {
        this.salary = salary;
    }
    
    /**
     * Employee address getter
     * 
     * @return address the address of employee
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="ADDR_ID", referencedColumnName="id", unique=true)
    public Address getAddress() {
        return address;
    }

    /**
     * 
     * @param address set address of employee
     */
    public void setAddress(Address address) {
        this.address = address;
    }
    
    /**
     * Employee phones getter
     * 
     * @return phones the list of phones owned by employee
     */
    @OneToMany(mappedBy="owner")
    public List<Phone> getPhones() {
        return phones;
    }
    
    /**
     * 
     * @param phone set single phone to employee
     */
    public void setPhones(Phone phone) {
        this.phones.add(phone);
        if(phone.getOwner()!=this) {
            phone.setOwner(this);
        }
    }

    /**
     * 
     * @param phoneList set several phones to employee
     */
    public void setPhones(List<Phone> phoneList) {
        this.phones = phoneList;
    }
    
    /**
     * Employee projects getter
     * 
     * @return projects the list of projects owned by employee
     */
    @ManyToMany
    @JoinTable(
            name="Emp_Proj",
            joinColumns = @JoinColumn(name="emp_id",referencedColumnName="ID"),
            inverseJoinColumns=@JoinColumn(name="PROJ_ID", referencedColumnName="ID")
            )
    public List<Project> getProjects() {
        return projects;
    }

    /**
     * 
     * @param projects set several projects to employee
     */ 
    public void setProjects(List<Project> projects) {
        this.projects = projects;
    }
}