package com.algonquincollege.cst8277.models;

import javax.annotation.Generated;
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="Dali", date="2019-03-29T01:40:26.998+0000")
@StaticMetamodel(Employee.class)
public class Employee_ extends ModelBase_ {
	public static volatile SingularAttribute<Employee, String> firstName;
	public static volatile SingularAttribute<Employee, String> lastName;
	public static volatile SingularAttribute<Employee, Double> salary;
	public static volatile SingularAttribute<Employee, Address> address;
	public static volatile ListAttribute<Employee, Phone> phones;
	public static volatile ListAttribute<Employee, Project> projects;
}
