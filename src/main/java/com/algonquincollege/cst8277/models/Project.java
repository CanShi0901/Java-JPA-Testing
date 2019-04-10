/**
 * File: Project.java
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
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

/**
 * The Project class demonstrates:
 * <ul>
 * <li>Generated Id
 * <li>Version locking
 * <li>ManyToMany mapping
 * </ul>
 */
@Entity
@Table(name="PROJECT")
public class Project extends ModelBase implements Serializable {
    /** explicit set serialVersionUID */
    private static final long serialVersionUID = 1L;

    /**
     * description of the project saved as a string
     */
    protected String description;
    /**
     * name of the project saved as a string
     */
    protected String name;

    /**
     * employees working on this project
     */
    protected List<Employee> employees;

    // JPA requires each @Entity class have a default constructor
    public Project() {
    }
    
    public Project(String name, String description) {
        this.setName(name);
        this.setDescription(description);
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
        if (!(obj instanceof Project)) {
            return false;
        }
        Project other = (Project)obj;
        if (id != other.id) {
            return false;
        }
        return true;
    }

    /**
     * Project description getter
     * 
     * @return description the description of project
     */
    @Column(name="description")
    public String getDescription() {
        return description;
    }

    /**
     * Project description setter
     * 
     * @param description set project description
     */
    public void setDescription(String description) {
        this.description = description;
    }
    
    /**
     * Project name getter
     * 
     * @return name the name of project
     */
    @Column(name="name")
    public String getName() {
        return name;
    }

    /**
     * Project name setter
     * 
     * @param name set project name
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * Project employee getter
     * 
     * @return employees the employees who is responsible for project
     */
    @ManyToMany(mappedBy="projects")
    public List<Employee> getEmployees() {
        return employees;
    }

    /**
     * Project employee setter
     * 
     * @param employees set employee who is responsible for porject
     */
    public void setEmployees(List<Employee> employees) {
        this.employees = employees;
    }

    
}