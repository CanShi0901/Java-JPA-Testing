/**
 * File: EmployeeTestSuite.java
 * Course materials (19W) CST 8277
 * (Students) @author: Can Shi 040806036 Zeyang Hu 040885680
 * (Modified) @date: 2019 03 13
 * (Professor) @author Mike Norman
 */
package com.algonquincollege.cst8277.models;

import static com.algonquincollege.cst8277.models.TestSuiteConstants.attachListAppender;
import static com.algonquincollege.cst8277.models.TestSuiteConstants.buildEntityManagerFactory;
import static com.algonquincollege.cst8277.models.TestSuiteConstants.detachListAppender;
import static org.hamcrest.core.StringStartsWith.startsWith;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.lang.invoke.MethodHandles;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.h2.tools.Server;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class EmployeeTestSuite implements TestSuiteConstants {

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

    private static final String SELECT_EMPLOYEE_1 =
        "SELECT ID, FIRSTNAME, LASTNAME, SALARY, VERSION, ADDR_ID FROM EMPLOYEE WHERE (ID = ?)";
    
    private static final String CREATE_EMPLOYEE_1 =
        "INSERT INTO EMPLOYEE EMPLOYEE (FIRSTNAME, LASTNAME, SALARY, VERSION, ADDR_ID)"
        + "VALUES(?,?,?,?,?)";
    
    
    /**
     * test no employees exist
     */
    @Test
    public void test_Employee_0_at_start() {
        EntityManager em = emf.createEntityManager();

        ListAppender<ILoggingEvent> listAppender = attachListAppender(eclipselinkSqlLogger, ECLIPSELINK_LOGGING_SQL);
        Employee emp1 = em.find(Employee.class, 1);
        detachListAppender(eclipselinkSqlLogger, listAppender);

        assertNull(emp1);
        List<ILoggingEvent> loggingEvents = listAppender.list;
        assertEquals(1, loggingEvents.size());
        assertThat(loggingEvents.get(0).getMessage(),
            startsWith(SELECT_EMPLOYEE_1));

        em.close();
    }

    // C-R-U-D lifecycle
    /**
     * test creating an employee
     */
    @Test
    public void test_Employee_1_Create() {
        EntityManager em = emf.createEntityManager();
        
        Employee employeeNew = new Employee();
        employeeNew.setFirstName("Jim");
        employeeNew.setLastName("Green");
        employeeNew.setSalary(2000);
        
        em.getTransaction().begin();
        em.persist(employeeNew);
        em.getTransaction().commit();
        
        Employee emp = em.find(Employee.class, employeeNew.getId());

        assertEquals("Jim", emp.getFirstName());
        assertEquals("Green", emp.getLastName());
        assertTrue(emp.getSalary()==2000);
        em.close();
    }
    
    /**
     * test finding a created employee
     */
    @Test
    public void test_Employee_2_Read() {
        EntityManager em = emf.createEntityManager();

        Query readQuery = em.createQuery("SELECT emp.firstName FROM Employee emp Where emp.firstName = :paraFirstName");
        readQuery.setParameter("paraFirstName","Jim");
        String empFirstName = (String) readQuery.getSingleResult();
        assertEquals("Jim", empFirstName);

        em.close();

    }    
    
    /**
     * test updating an employee
     */
    @Test
    public void test_Employee_3_Update() {
        EntityManager em = emf.createEntityManager();
        
        em.getTransaction().begin();
        Query updateQuery = em.createQuery("UPDATE Employee emp SET "
                                            + "emp.firstName = :paraFirstName, "
                                            + "emp.lastName = :paraLastName, "
                                            + "emp.salary = :paraSalary "
                                            + "Where emp.id = :paraId");
        updateQuery.setParameter("paraFirstName", "Alvin");
        updateQuery.setParameter("paraLastName", "Smith");
        updateQuery.setParameter("paraSalary", 2000);
        updateQuery.setParameter("paraId", 1);
        int rowsUpdated = updateQuery.executeUpdate();
        
        em.getTransaction().commit();  
        
        Employee targetEmp = em.find(Employee.class, 1);
        
        assertTrue(rowsUpdated==1);
        assertEquals("Alvin", targetEmp.getFirstName());
        assertEquals("Smith", targetEmp.getLastName());
        assertTrue(targetEmp.getSalary()== 2000);
        
        em.close();
    }

    /**
     * test deleting an employee
     */
    @Test
    public void test_Employee_4_Delete() {
        EntityManager em = emf.createEntityManager();
        
        em.getTransaction().begin();
        Query deleteQuery = em.createQuery("Delete From Employee emp Where emp.id = :paraId");
        deleteQuery.setParameter("paraId", 1);
        int rowaffected = deleteQuery.executeUpdate();
        em.getTransaction().commit();
        
        Employee targetEmp = em.find(Employee.class, 1);
        assertTrue(rowaffected==1);
        assertNull(targetEmp);
        
        em.close();
    }

    /**
     * find emp by highest salary
     */
    @Test
    public void test_Employee_5_find_by_highest_salary() {
        EntityManager em = emf.createEntityManager();
        
        for(int i =1;i<6; i++) {
            Employee employeeNew = new Employee();
            //employeeNew.setId(i);
            employeeNew.setFirstName("Emp_first_name"+i);
            employeeNew.setLastName("Emp_last_name"+i);
            employeeNew.setSalary(1000+i*100);
            
            em.getTransaction().begin();
            em.persist(employeeNew);
            em.getTransaction().commit();
        }
        
        Query highestSalaryQuery = em.createQuery("SELECT emp.salary from Employee emp WHERE emp.salary = (SELECT MAX(emp.salary) FROM Employee emp)");
        double highlestSalary = (double) highestSalaryQuery.getSingleResult();
        
        assertTrue(highlestSalary == 1500);
        
        em.close();
    }
    
    /**
     * find employtee using salary greater than...
     */
    @Test
    public void test_Employee_6_CriteriaBuilder_salary_gt() {
        
        EntityManager em = emf.createEntityManager();
        
        CriteriaBuilder cb = em.getCriteriaBuilder();
        
        CriteriaQuery<Employee> cq = cb.createQuery(Employee.class);
        
        Root<Employee> employee = cq.from(Employee.class);
        
        Predicate condition = cb.gt(employee.get(Employee_.salary), 1200);
        cq.select(employee).where(condition);
        
        TypedQuery<Employee> typedQuery = em.createQuery(cq);
        List<Employee> result = typedQuery.getResultList();
        
        assertEquals("Emp_first_name3", result.get(0).getFirstName());
        assertEquals("Emp_last_name3", result.get(0).getLastName());
        assertTrue(result.get(0).salary == 1300);
        
        em.close();
    }
    
    /**
     * find an employee using salary equal to...
     */
    @Test
    public void test_Employee_7_CriteriaBuilder_salary_equal() {
        
        EntityManager em = emf.createEntityManager();
        
        CriteriaBuilder cb = em.getCriteriaBuilder();
        
        CriteriaQuery<Employee> cq = cb.createQuery(Employee.class);
        
        Root<Employee> employee = cq.from(Employee.class);
        
        Predicate condition = cb.equal(employee.get(Employee_.salary), 1500);
        
        cq.select(employee).where(condition);
        
        TypedQuery<Employee> typedQuery = em.createQuery(cq);
        List<Employee> result = typedQuery.getResultList();

        assertEquals("Emp_first_name5", result.get(0).getFirstName());
        
        em.close();
    }
    
    /**
     * find an employee using greatest
     */
    @Test
    public void test_Employee_8_CriteriaBuilder_salary_greatest() {
        
        EntityManager em = emf.createEntityManager();
        
        CriteriaBuilder cb = em.getCriteriaBuilder();
        
        CriteriaQuery<Employee> cq = cb.createQuery(Employee.class);
        
        Root<Employee> employee = cq.from(Employee.class);
        
        
        Subquery<Double> maxSubQuery = cq.subquery(Double.class);
        
        Root<Employee> fromEmployee = maxSubQuery.from(Employee.class);
              
        Expression<Double> expression = cb.greatest(fromEmployee.get(Employee_.salary));
        
        maxSubQuery.select(expression);
        
        
        Predicate condition = cb.equal(employee.get(Employee_.salary), maxSubQuery);
        
        cq.select(employee).where(condition);
        
        TypedQuery<Employee> typedQuery = em.createQuery(cq);
        List<Employee> result = typedQuery.getResultList();
        
        assertTrue(result.get(0).getSalary()==(double)1500);
        
        em.close();
    }
    
    /**
     * find employee salary between a range
     */
    @Test
    public void test_Employee_9_CriteriaBuilder_salary_between() {
        
        EntityManager em = emf.createEntityManager();
        
        CriteriaBuilder cb = em.getCriteriaBuilder();
        
        CriteriaQuery<Employee> cq = cb.createQuery(Employee.class);
        
        Root<Employee> employee = cq.from(Employee.class);
        
        
        Predicate range = cb.between(employee.get(Employee_.salary), 1200d, 1400d);
        
        cq.select(employee).where(range);
        
        List<Employee> result = em.createQuery(cq).getResultList();
        
        
        assertTrue(result.size()==3);
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