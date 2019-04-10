package com.algonquincollege.cst8277.models;

import javax.annotation.Generated;
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="Dali", date="2019-03-16T20:21:02.147+0000")
@StaticMetamodel(Project.class)
public class Project_ extends ModelBase_ {
	public static volatile SingularAttribute<Project, String> description;
	public static volatile SingularAttribute<Project, String> name;
	public static volatile ListAttribute<Project, Employee> employees;
}
