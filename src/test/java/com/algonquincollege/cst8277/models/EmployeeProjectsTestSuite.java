/**
 * File: EmployeePhonesSuite.java
 * Course materials (19W) CST 8277
 * (Students) @author: Can Shi 040806036 Zeyang Hu 040885680
 * (Modified) @date: 2019 03 13
 * (Professor) @author Mike Norman
 */
package com.algonquincollege.cst8277.models;

import static com.algonquincollege.cst8277.models.TestSuiteConstants.buildEntityManagerFactory;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.EntityType;

import org.h2.tools.Server;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class EmployeeProjectsTestSuite implements TestSuiteConstants {

    private static final Class<?> _thisClaz = MethodHandles.lookup().lookupClass();
    private static final Logger logger = LoggerFactory.getLogger(_thisClaz);
    private static final ch.qos.logback.classic.Logger eclipselinkSqlLogger =
            (ch.qos.logback.classic.Logger)LoggerFactory.getLogger(ECLIPSELINK_LOGGING_SQL);

    // test fixture(s)
    public static EntityManagerFactory emf;
    public static Server server;

    @BeforeClass
    public static void oneTimeSetUp() {
        try {
            logger.debug("oneTimeSetUp");
            // create in-process H2 server so we can 'see' into database
            // use "jdbc:h2:tcp://localhost:9092/mem:assignment3-testing" in Db Perspective
            // (connection in .dbeaver-data-sources.xml so should be immediately useable
            server = Server.createTcpServer().start();
            emf = buildEntityManagerFactory(_thisClaz.getSimpleName());
        }
        catch (Exception e) {
            logger.error("something went wrong building EntityManagerFactory", e);
        }
    }

    /**
     * using metamodel to test project1 has employee1 working on it
     */
    @Test
    public void test00_OneProjectAddOneEmployee() {
        System.out.println("Running test 0.");
        EntityManager em = emf.createEntityManager();
        Project project1;
        Employee employee0;

        project1 = new Project("AAA","This is project AAA");
        employee0 = new Employee("Arron","Smith", 10000.00);

        List<Employee> employees = new ArrayList<>();
        List<Project> projects = new ArrayList<>();

        employees.add(employee0);
        projects.add(project1);

        project1.setEmployees(employees);
        employee0.setProjects(projects);

        em.getTransaction().begin();
        em.persist(employee0);
        em.persist(project1);
        em.getTransaction().commit();

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Project> cq = cb.createQuery(Project.class);
        Root<Project> project = cq.from(Project.class);
        cq.select(project);
        TypedQuery<Project> typedQuery = em.createQuery(cq);
        List<Project> result = typedQuery.getResultList();        

        assertEquals("AAA", result.get(0).getName());
        assertEquals("Arron", result.get(0).getEmployees().get(0).getFirstName());
        em.close();
    }

    /**
     * using jpql to test an updated employee can be found through project
     */
    @Test
    public void test01_OneProjectOneEmployeeUpdated() {
        System.out.println("Running test 1.");

        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();

        Query updateEmployee = em.createQuery("UPDATE Employee e SET e.firstName = :newFirstName "
                + "WHERE e.firstName = :oldFirstName");
        updateEmployee.setParameter("newFirstName","Apple");
        updateEmployee.setParameter("oldFirstName", "Arron");

        int rowsUpdated = updateEmployee.executeUpdate();
        System.out.println(rowsUpdated + " row(s) were updaed. ");

        em.getTransaction().commit();

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Project> cq = cb.createQuery(Project.class);
        Root<Project> project = cq.from(Project.class);
        cq.select(project);
        TypedQuery<Project> typedQuery = em.createQuery(cq);
        List<Project> result = typedQuery.getResultList();        

        assertEquals("AAA", result.get(0).getName());
        assertEquals("Apple", result.get(0).getEmployees().get(0).getFirstName());
        em.close();
    }

    /**
     * using jpql to delete the only project from project table then check Project table has no rows anymore
     */
    @Test
    public void test02_SingleProjectDeleteOneEmployee() {
        System.out.println("Running test 2.");

        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();

        Query query = em.createQuery("DELETE FROM Employee e WHERE e.firstName = :name");
        query.setParameter("name", "Apple");

        int rowsDeleted = query.executeUpdate();
        System.out.print("Rows updated: " + rowsDeleted);
        em.getTransaction().commit();

        List<Employee> e = em.createQuery("Select e from Employee e").getResultList();
        assertEquals(0, e.size());

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Project> cq = cb.createQuery(Project.class);
        Root<Project> project = cq.from(Project.class);
        cq.select(project);
        TypedQuery<Project> typedQuery = em.createQuery(cq);
        List<Project> result = typedQuery.getResultList(); 

        result.get(0).getEmployees().clear();
        assertEquals("AAA", result.get(0).getName());
        assertEquals(0, result.get(0).getEmployees().size());

        em.close();
    }

    /**
     * using metamodel to test project1 have multiple employees working on it
     */
    @Test
    public void test03_OneProjectAddMultipleEmployees() {
        System.out.println("Running test 3.");

        EntityManager em = emf.createEntityManager();

        List<Employee> employees = new ArrayList<>();
        List<Project> projects = new ArrayList<>();

        Employee employee1;
        Employee employee2;
        Employee employee3;

        employee1 = new Employee("Arron","Smith", 10000.00);
        employee2 = new Employee("Bill","Smith", 20000.00);
        employee3 = new Employee("Carl","Smith", 30000.00);

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Project> cq = cb.createQuery(Project.class);
        Root<Project> project = cq.from(Project.class);
        cq.select(project);
        TypedQuery<Project> typedQuery = em.createQuery(cq);
        List<Project> result = typedQuery.getResultList(); 

        Project project1 = result.get(0);

        employees.add(employee1);
        employees.add(employee2);
        employees.add(employee3);
        projects.add(project1);

        project1.setEmployees(employees);
        employee1.setProjects(projects);
        employee2.setProjects(projects);
        employee3.setProjects(projects);


        em.getTransaction().begin();
        em.persist(employee1);
        em.persist(employee2);
        em.persist(employee3);
        em.persist(project1);
        em.getTransaction().commit();

        assertEquals("AAA", result.get(0).getName());
        assertEquals("Arron", result.get(0).getEmployees().get(0).getFirstName());
        assertEquals("Bill", result.get(0).getEmployees().get(1).getFirstName());
        assertEquals("Carl", result.get(0).getEmployees().get(2).getFirstName());

        em.close();
    }

    /**
     * using jpql to test multiple updated employees can be found through one project
     */
    @Test
    public void test04_OneProjectManyEmployeesUpdated() {
        System.out.println("Running test 4.");

        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();

        Query updateEmployee1 = em.createQuery("UPDATE Employee e SET e.firstName = :newFirstName "
                + "WHERE e.firstName = :oldFirstName");
        updateEmployee1.setParameter("newFirstName","Ace");
        updateEmployee1.setParameter("oldFirstName", "Arron");
        int rowsUpdated = updateEmployee1.executeUpdate();
        System.out.println(rowsUpdated + " row(s) were updated. ");

        Query updateEmployee2 = em.createQuery("UPDATE Employee e SET e.firstName = :newFirstName "
                + "WHERE e.firstName = :oldFirstName");
        updateEmployee2.setParameter("newFirstName","Banana");
        updateEmployee2.setParameter("oldFirstName", "Bill");
        int rowsUpdated2 = updateEmployee2.executeUpdate();
        System.out.println(rowsUpdated2 + " row(s) were updated. ");

        Query updateEmployee3 = em.createQuery("UPDATE Employee e SET e.firstName = :newFirstName "
                + "WHERE e.firstName = :oldFirstName");
        updateEmployee3.setParameter("newFirstName","Carrot");
        updateEmployee3.setParameter("oldFirstName", "Carl");
        int rowsUpdated3 = updateEmployee3.executeUpdate();
        System.out.println(rowsUpdated3 + " row(s) were updated. ");

        em.getTransaction().commit();

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Project> cq = cb.createQuery(Project.class);
        Root<Project> project = cq.from(Project.class);
        cq.select(project);
        TypedQuery<Project> typedQuery = em.createQuery(cq);
        List<Project> result = typedQuery.getResultList();       
        
        CriteriaBuilder cb1 = em.getCriteriaBuilder();
        CriteriaQuery<Employee> cq1 = cb1.createQuery(Employee.class);
        Root<Employee> employee1 = cq1.from(Employee.class);
        cq1.select(employee1);
        TypedQuery<Employee> typedQuery1 = em.createQuery(cq1);
        List<Employee> result1 = typedQuery1.getResultList(); 

        assertEquals("AAA", result.get(0).getName());
        assertTrue(result.get(0).getEmployees().containsAll(result1));
        //assertEquals("Ace", result.get(0).getEmployees().get(0).getFirstName());
        //assertEquals("Banana", result.get(0).getEmployees().get(1).getFirstName());
        //assertEquals("Carrot", result.get(0).getEmployees().get(2).getFirstName());

        em.close();
    }

    /**
     *  delete one employee from project1
     */
    @Test
    public void test05_SingleProjectDeleteOneEmployee() {
        System.out.println("Running test 5.");

        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();

        Employee e = em.createQuery("SELECT e from Employee e WHERE e.firstName = :firstName", Employee.class)
                .setParameter("firstName", "Carrot").getSingleResult();
        Project p = em.createQuery("SELECT p from Project p WHERE p.name = :name", Project.class)
                .setParameter("name", "AAA").getSingleResult();
        p.getEmployees().remove(e);
        e.getProjects().remove(p);

        CriteriaBuilder cb = em.getCriteriaBuilder();

        CriteriaQuery<Project> cq = cb.createQuery(Project.class);
        Root<Project> project = cq.from(Project.class);
        cq.select(project);
        TypedQuery<Project> typedQuery = em.createQuery(cq);
        List<Project> result = typedQuery.getResultList();        

        CriteriaBuilder cb1 = em.getCriteriaBuilder();
        CriteriaQuery<Employee> cq1 = cb1.createQuery(Employee.class);
        Root<Employee> employee1 = cq1.from(Employee.class);
        cq1.select(employee1);
        TypedQuery<Employee> typedQuery1 = em.createQuery(cq1);
        List<Employee> result1 = typedQuery1.getResultList(); 

        assertEquals("AAA", result.get(0).getName());
        assertFalse(result.get(0).getEmployees().containsAll(result1));
        assertEquals(2, result.get(0).getEmployees().size());
        em.close();
    }

    /**
     *  delete all employees from project1
     */
    @Test
    public void test06_SingleProjectDeleteAllEmployees() {
        System.out.println("Running test 6.");

        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();

        CriteriaBuilder cb = em.getCriteriaBuilder();

        List<Employee> e = em.createQuery("SELECT e from Employee e", Employee.class).getResultList();
        Project p = em.createQuery("SELECT p from Project p WHERE p.name = :name", Project.class)
                .setParameter("name", "AAA").getSingleResult();
        p.getEmployees().removeAll(e);

        CriteriaQuery<Project> cq = cb.createQuery(Project.class);
        Root<Project> project = cq.from(Project.class);
        cq.select(project);
        TypedQuery<Project> typedQuery = em.createQuery(cq);
        List<Project> result = typedQuery.getResultList();        

        assertEquals("AAA", result.get(0).getName());
        assertEquals(0, result.get(0).getEmployees().size());
        em.close();
    }

    /**
     * using metamodel to test multiple projects can add multiple employees working on them
     */
    @Test
    public void test07_MultipleProjectsAddMultipleEmployees() {
        System.out.println("Running test 7.");

        Project project2, project3;
        Employee employee1, employee2, employee3;

        project2 = new Project("BBB", "This is project BBB");
        project3 = new Project("CCC", "This is project CCC");
        employee1 = new Employee("Arron","Smith", 10000.00);
        employee2 = new Employee("Bill","Smith", 20000.00);
        employee3 = new Employee("Carl","Smith", 30000.00);

        List<Employee> employees1 = new ArrayList<>();
        employees1.add(employee1);

        List<Employee> employees2 = new ArrayList<>();
        employees2.add(employee1);
        employees2.add(employee2);

        List<Employee> employees3 = new ArrayList<>();
        employees3.add(employee1);
        employees3.add(employee2);
        employees3.add(employee3);

        EntityManager em = emf.createEntityManager();

        CriteriaBuilder cb0 = em.getCriteriaBuilder();
        CriteriaQuery<Project> cq0 = cb0.createQuery(Project.class);
        Root<Project> project0 = cq0.from(Project.class);
        cq0.select(project0);
        TypedQuery<Project> typedQuery0 = em.createQuery(cq0);
        List<Project> result0 = typedQuery0.getResultList(); 

        Project project1 = result0.get(0);

        List<Project> projects1 = new ArrayList<>();
        projects1.add(project1);

        List<Project> projects2 = new ArrayList<>();
        projects2.add(project1);
        projects2.add(project2);

        List<Project> projects3 = new ArrayList<>();
        projects3.add(project1);
        projects3.add(project2);
        projects3.add(project3);

        project1.setEmployees(employees1); //project1 has a single employee (1)
        project2.setEmployees(employees2); //project2 has two employees (1,2)
        project3.setEmployees(employees3); //project3 has three employees (1, 2, 3)

        em.getTransaction().begin();
        em.persist(employee1);
        em.persist(employee2);
        em.persist(employee3);
        em.persist(project1);
        em.persist(project2);
        em.persist(project3);
        em.getTransaction().commit();

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Project> cq = cb.createQuery(Project.class);
        Root<Project> project = cq.from(Project.class);
        cq.select(project);
        TypedQuery<Project> typedQuery = em.createQuery(cq);
        List<Project> result = typedQuery.getResultList();

        for (int i = 0; i <3; i ++) {
            switch (result.get(i).getName()) {
            case "AAA":
                assertEquals("Arron", result.get(i).getEmployees().get(0).getFirstName());
                assertTrue(result.get(i).getEmployees().size() == 1);
                break;
            case "BBB": 
                assertEquals("Arron", result.get(i).getEmployees().get(0).getFirstName());
                assertEquals("Bill", result.get(i).getEmployees().get(1).getFirstName());
                assertEquals(2, result.get(i).getEmployees().size());
                break;
            case "CCC":
                assertEquals("Arron", result.get(i).getEmployees().get(0).getFirstName());
                assertEquals("Bill", result.get(i).getEmployees().get(1).getFirstName());
                assertEquals("Carl", result.get(i).getEmployees().get(2).getFirstName());
                assertEquals(3, result.get(i).getEmployees().size());
                break;
            }
        }
        em.close();
    }

    /**
     * using jpql to test one updated employee can be found through many projects
     */
    @Test
    public void test08_ManyProjectsOneEmployeeUpdated() {
        System.out.println("Running test 8.");

        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();

        Query updateEmployee1 = em.createQuery("UPDATE Employee e SET e.firstName = :newFirstName "
                + "WHERE e.firstName = :oldFirstName");
        updateEmployee1.setParameter("newFirstName","Apple");
        updateEmployee1.setParameter("oldFirstName", "Arron");
        int rowsUpdated = updateEmployee1.executeUpdate();
        System.out.println(rowsUpdated + " row(s) were updated. ");

        em.getTransaction().commit();

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Project> cq = cb.createQuery(Project.class);
        Root<Project> project = cq.from(Project.class);
        cq.select(project);
        TypedQuery<Project> typedQuery = em.createQuery(cq);
        List<Project> result = typedQuery.getResultList();        

        for (int i = 0; i <3; i ++) {
            switch (result.get(i).getName()) {
            case "AAA":
                assertEquals("Apple", result.get(i).getEmployees().get(0).getFirstName());
                assertTrue(result.get(i).getEmployees().size() == 1);
                break;
            case "BBB": 
                assertEquals("Apple", result.get(i).getEmployees().get(0).getFirstName());
                assertEquals("Bill", result.get(i).getEmployees().get(1).getFirstName());
                assertEquals(2, result.get(i).getEmployees().size());
                break;
            case "CCC":
                assertEquals("Apple", result.get(i).getEmployees().get(0).getFirstName());
                assertEquals("Bill", result.get(i).getEmployees().get(1).getFirstName());
                assertEquals("Carl", result.get(i).getEmployees().get(2).getFirstName());
                assertEquals(3, result.get(i).getEmployees().size());
                break;
            }
        }
        em.close();
    }

    /**
     * using jpql to test many updated employees can be found through many projects
     */
    @Test
    public void test09_ManyProjectsManyEmployeesUpdated() {
        System.out.println("Running test 9.");

        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();

        Query updateEmployee2 = em.createQuery("UPDATE Employee e SET e.firstName = :newFirstName "
                + "WHERE e.firstName = :oldFirstName");
        updateEmployee2.setParameter("newFirstName","Banana");
        updateEmployee2.setParameter("oldFirstName", "Bill");
        int rowsUpdated = updateEmployee2.executeUpdate();
        System.out.println(rowsUpdated + " row(s) were updated. ");

        Query updateEmployee3 = em.createQuery("UPDATE Employee e SET e.firstName = :newFirstName "
                + "WHERE e.firstName = :oldFirstName");
        updateEmployee3.setParameter("newFirstName","Carrot");
        updateEmployee3.setParameter("oldFirstName", "Carl");
        int rowsUpdated2 = updateEmployee3.executeUpdate();
        System.out.println(rowsUpdated2 + " row(s) were updated. ");

        em.getTransaction().commit();

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Project> cq = cb.createQuery(Project.class);
        Root<Project> project = cq.from(Project.class);
        cq.select(project);
        TypedQuery<Project> typedQuery = em.createQuery(cq);
        List<Project> result = typedQuery.getResultList();        

        for (int i = 0; i <3; i ++) {
            switch (result.get(i).getName()) {
            case "AAA":
                assertEquals("Apple", result.get(i).getEmployees().get(0).getFirstName());
                assertTrue(result.get(i).getEmployees().size() == 1);
                break;
            case "BBB": 
                assertEquals("Apple", result.get(i).getEmployees().get(0).getFirstName());
                assertEquals("Banana", result.get(i).getEmployees().get(1).getFirstName());
                assertEquals(2, result.get(i).getEmployees().size());
                break;
            case "CCC":
                assertEquals("Apple", result.get(i).getEmployees().get(0).getFirstName());
                assertEquals("Banana", result.get(i).getEmployees().get(1).getFirstName());
                assertEquals("Carrot", result.get(i).getEmployees().get(2).getFirstName());
                assertEquals(3, result.get(i).getEmployees().size());
                break;
            }
        }
        em.close();
    }

    /**
     *  remove employee1 from one of the 3rd project
     */
    @Test
    public void test10_ManyProjectsOneEmployeeRemovedFromOneProject() {
        System.out.println("Running test 10.");

        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();

        Project p = em.createQuery("SELECT p from Project p WHERE p.name = :name", Project.class)
                .setParameter("name", "CCC").getSingleResult();
        p.getEmployees().remove(0);

        CriteriaBuilder cb = em.getCriteriaBuilder();

        CriteriaQuery<Project> cq = cb.createQuery(Project.class);
        Root<Project> project = cq.from(Project.class);
        cq.select(project);
        TypedQuery<Project> typedQuery = em.createQuery(cq);
        List<Project> result = typedQuery.getResultList();        

        for (int i = 0; i <3; i ++) {
            switch (result.get(i).getName()) {
            case "AAA":
                assertEquals("Apple", result.get(i).getEmployees().get(0).getFirstName());
                assertTrue(result.get(i).getEmployees().size() == 1);
                break;
            case "BBB": 
                assertEquals("Apple", result.get(i).getEmployees().get(0).getFirstName());
                assertEquals("Banana", result.get(i).getEmployees().get(1).getFirstName());
                assertEquals(2, result.get(i).getEmployees().size());
                break;
            case "CCC":
                assertEquals("Banana", result.get(i).getEmployees().get(0).getFirstName());
                assertEquals("Carrot", result.get(i).getEmployees().get(1).getFirstName());
                assertEquals(2, result.get(i).getEmployees().size());
                break;
            }
        }
        em.close();
    }

    /**
     *  remove employee2 and employee3 from one of the 2nd and 3rd projects
     */
    @Test
    public void test11_MultipleEmployeesRemovedFromMultipleProjects() {
        System.out.println("Running test 11.");

        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();

        Project p2 = em.createQuery("SELECT p from Project p WHERE p.name = :name", Project.class)
                .setParameter("name", "BBB").getSingleResult();
        p2.getEmployees().clear();;

        Project p3 = em.createQuery("SELECT p from Project p WHERE p.name = :name", Project.class)
                .setParameter("name", "CCC").getSingleResult();
        p3.getEmployees().clear();;

        CriteriaBuilder cb = em.getCriteriaBuilder();

        CriteriaQuery<Project> cq = cb.createQuery(Project.class);
        Root<Project> project = cq.from(Project.class);
        cq.select(project);
        TypedQuery<Project> typedQuery = em.createQuery(cq);
        List<Project> result = typedQuery.getResultList();        

        for (int i = 0; i <3; i ++) {
            switch (result.get(i).getName()) {
            case "AAA":
                assertEquals("Apple", result.get(i).getEmployees().get(0).getFirstName());
                assertTrue(result.get(i).getEmployees().size() == 1);
                break;
            case "BBB": 
                assertEquals(0, result.get(i).getEmployees().size());
                break;
            case "CCC":
                assertEquals(0, result.get(i).getEmployees().size());
                break;
            }
        }
        em.close();
    }

    /**
     *  Re-add employee1 to project3, then check employee1 has 2 projects instead of 1
     */
    @Test
    public void test12_CheckEmployeeHasMultipleProjects() {
        System.out.println("Running test 12.");

        EntityManager em = emf.createEntityManager();

        em.getTransaction().begin();
        
        CriteriaBuilder cb0 = em.getCriteriaBuilder();
        CriteriaQuery<Project> cq0 = cb0.createQuery(Project.class);
        Root<Project> project0 = cq0.from(Project.class);
        cq0.select(project0);
        TypedQuery<Project> typedQuery0 = em.createQuery(cq0);
        List<Project> result0 = typedQuery0.getResultList(); 

        Project project3 = result0.get(2);

        CriteriaBuilder cb1 = em.getCriteriaBuilder();
        CriteriaQuery<Employee> cq1 = cb1.createQuery(Employee.class);
        Root<Employee> employee1 = cq1.from(Employee.class);
        cq1.select(employee1);
        TypedQuery<Employee> typedQuery1 = em.createQuery(cq1);
        List<Employee> result1 = typedQuery1.getResultList(); 

        Employee emp1 = result1.get(0);

        project3.getEmployees().add(emp1);
        emp1.getProjects().add(project3);

        
        em.getTransaction().commit();

        CriteriaBuilder cb2 = em.getCriteriaBuilder();
        CriteriaQuery<Employee> cq2 = cb2.createQuery(Employee.class);
        Root<Employee> employee2 = cq2.from(Employee.class);
        cq1.select(employee2);
        TypedQuery<Employee> typedQuery2 = em.createQuery(cq2);
        List<Employee> result2 = typedQuery2.getResultList(); 

        assertEquals(2, result2.get(0).getProjects().size());
        em.close();
    }

    /**
     * update project1 and find it through empployee1
     */
    @Test
    public void test13_OneProjectUpdated() {
        System.out.println("Running test 13.");

        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();

        Query updateProject1 = em.createQuery("UPDATE Project p SET p.name = :newName "
                + "WHERE p.name = :oldName");
        updateProject1.setParameter("newName","First");
        updateProject1.setParameter("oldName", "AAA");

        int rowsUpdated = updateProject1.executeUpdate();
        System.out.println(rowsUpdated + " row(s) were updaed. ");

        em.getTransaction().commit();

        CriteriaBuilder cb1 = em.getCriteriaBuilder();
        CriteriaQuery<Employee> cq1 = cb1.createQuery(Employee.class);
        Root<Employee> employee1 = cq1.from(Employee.class);
        cq1.select(employee1);
        TypedQuery<Employee> typedQuery1 = em.createQuery(cq1);
        List<Employee> result1 = typedQuery1.getResultList(); 

        Employee emp1 = result1.get(0);     

        assertEquals("First", emp1.getProjects().get(0).getName());
        em.close();
    }

    /**
     * update project2 and project3 and find it through empployee2
     */
    @Test
    public void test14_OneProjectUpdated() {
        System.out.println("Running test 14.");

        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();

        Query updateProject2 = em.createQuery("UPDATE Project p SET p.name = :newName "
                + "WHERE p.name = :oldName");
        updateProject2.setParameter("newName","Second");
        updateProject2.setParameter("oldName", "BBB");

        int rowsUpdated = updateProject2.executeUpdate();
        System.out.println(rowsUpdated + " row(s) were updaed. ");

        Query updateProject3 = em.createQuery("UPDATE Project p SET p.name = :newName "
                + "WHERE p.name = :oldName");
        updateProject3.setParameter("newName","Third");
        updateProject3.setParameter("oldName", "CCC");

        int rowsUpdated2 = updateProject3.executeUpdate();
        System.out.println(rowsUpdated2 + " row(s) were updaed. ");

        em.getTransaction().commit();

        CriteriaBuilder cb1 = em.getCriteriaBuilder();
        CriteriaQuery<Employee> cq1 = cb1.createQuery(Employee.class);
        Root<Employee> employee1 = cq1.from(Employee.class);
        cq1.select(employee1);
        TypedQuery<Employee> typedQuery1 = em.createQuery(cq1);
        List<Employee> result1 = typedQuery1.getResultList(); 

        Employee emp2 = result1.get(1);

        CriteriaBuilder cb0 = em.getCriteriaBuilder();
        CriteriaQuery<Project> cq0 = cb0.createQuery(Project.class);
        Root<Project> project0 = cq0.from(Project.class);
        cq0.select(project0);
        TypedQuery<Project> typedQuery0 = em.createQuery(cq0);
        List<Project> result0 = typedQuery0.getResultList(); 

        emp2.getProjects().add(result0.get(1));
        emp2.getProjects().add(result0.get(2));

        assertEquals(3, emp2.getProjects().size());

        em.close();
    }

    @AfterClass
    public static void oneTimeTearDown() {
        logger.debug("oneTimeTearDown");
        if (emf != null) {
            emf.close();
        }
        if (server != null) {
            server.stop();
        }
    }

}
