/**
 * File: Phone.java
 * Course materials (19W) CST 8277
 * (Students) @author: Can Shi 040806036 Zeyang Hu xxxxxxxxx
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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * The Phone class demonstrates:
 * <ul>
 * <li>Generated Id
 * <li>Version locking
 * <li>ManyToOne mapping
 * </ul>
 */
@Entity
@Table(name="PHONE")
public class Phone extends ModelBase implements Serializable {
    /** explicit set serialVersionUID */
    private static final long serialVersionUID = 1L;

    /**
     * areaCode saved as a string
     */
    protected String areaCode;
    /**
     * phone number saved as a string
     */
    protected String phoneNumber;
    /**
     * type of phone number saved as a string
     */
    protected String type;

    /**
     * employee having this phone information 
     */
    protected Employee owner;
    
    // JPA requires each @Entity class have a default constructor
    public Phone() {
        super();
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
        if (!(obj instanceof Phone)) {
            return false;
        }
        Phone other = (Phone)obj;
        if (id != other.id) {
            return false;
        }
        return true;
    }
    
    /**
     * phone area code getter
     * 
     * @return areaCode the areacode of the phone
     */
    @Column(name="areacode")
    public String getAreaCode() {
        return areaCode;
    }

    /**
     * Phone areaCode setter
     * 
     * @param areaCode set phone areaCode
     */
    public void setAreaCode(String areaCode) {
        this.areaCode = areaCode;
    }
    
    /**
     * phone number getter
     * 
     * @return phoneNumber the phoneNumber of the phone
     */
    @Column(name="phonenumber")
    public String getPhoneNumber() {
        return phoneNumber;
    }

    /**
     * Phone phoneNumber setter
     * 
     * @param phoneNumber set phone phoneNumber
     */
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    
    /**
     * phone type getter
     * 
     * @return type the type of phone
     */
    @Column(name="type")
    public String getType() {
        return type;
    }

    /**
     * Phone type setter
     * 
     * @param type set phone type
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * phone owner getter
     * 
     * @return owner the owner of phone
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="owning_emp_id")
    public Employee getOwner() {
        return owner;
    }
    
    /**
     * Phone owner setter
     * 
     * @param owner set phone owner
     */
    public void setOwner(Employee owner) {
        this.owner = owner;
        if(!owner.getPhones().contains(this)) {
            owner.getPhones().add(this);
        }
    }
    
    
}