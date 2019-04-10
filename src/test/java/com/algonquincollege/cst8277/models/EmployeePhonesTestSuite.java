/**
 * File: EmployeePhonesTestSuite.java
 * Course materials (19W) CST 8277
 * (Students) @author: Can Shi 040806036 Zeyang Hu 040885680
 * (Modified) @date: 2019 03 13
 * (Professor) @author Mike Norman
 */
package com.algonquincollege.cst8277.models;

import static com.algonquincollege.cst8277.models.TestSuiteConstants.buildEntityManagerFactory;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
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
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.h2.tools.Server;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class EmployeePhonesTestSuite implements TestSuiteConstants {

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
     * create a phone
     */
    @Test
    public void test_Phone_1_create() {
        
        EntityManager em = emf.createEntityManager();
        
        Employee employeeOne = new Employee();
        employeeOne.setFirstName("Jim");
        employeeOne.setLastName("Green");
        employeeOne.setSalary(2000);
       
        Phone phone = new Phone();
        phone.setAreaCode("613");
        phone.setPhoneNumber("0000001");
        phone.setType("Landline");
        
        employeeOne.setPhones(phone);
        phone.setOwner(employeeOne);
        
        em.getTransaction().begin();
        em.persist(employeeOne);
        em.persist(phone);
        em.getTransaction().commit();

        Query checkIfCreateQuery = em.createQuery("SELECT COUNT(phone) FROM Phone phone Where phone.id = :paraPhoneId");
        checkIfCreateQuery.setParameter("paraPhoneId", 1);
        Long countNumber = (Long) checkIfCreateQuery.getSingleResult();
        
        assertTrue(countNumber==1l);
        em.close();
    }

    /**
     * find a created phone
     */
    @Test
    public void test_Phone_2_Read() {
        EntityManager em = emf.createEntityManager();
        
        Query readQuery = em.createQuery("SELECT phone From Phone phone where phone.id = :paraPhoneId");
        readQuery.setParameter("paraPhoneId", 1);
        Phone selectedPhone = (Phone) readQuery.getSingleResult();
        
        assertNotNull(selectedPhone);
        assertEquals("613", selectedPhone.getAreaCode());
        assertEquals("0000001", selectedPhone.getPhoneNumber());
        assertEquals("Landline", selectedPhone.getType());
        assertEquals("Jim",selectedPhone.getOwner().getFirstName());
        assertEquals("Green", selectedPhone.getOwner().getLastName());
        em.close();
    }
    
    /**
     * update a created phone
     */
    @Test
    public void test_Phone_3_update() {
        EntityManager em = emf.createEntityManager();
        
        Query updateQuery = em.createQuery("Update Phone phone "
                                        + "SET phone.areaCode = :paraAreaCode "
                                        + "WHERE phone.owner.firstName = :paraOwnerFirstName "
                                        + "And phone.owner.lastName = :paraOwnerLastName");

        updateQuery.setParameter("paraAreaCode", "519");
        updateQuery.setParameter("paraOwnerFirstName", "Jim");
        updateQuery.setParameter("paraOwnerLastName", "Green");
        
        em.getTransaction().begin();
        int rowsUpdated = updateQuery.executeUpdate();
        em.getTransaction().commit();
        
        Query checkifUpdateQuery = em.createQuery("SELECT phone From Phone phone WHERE phone.owner.firstName = :paraOwnerFirstName And "
                                                + "phone.owner.lastName = :paraOwnerLastName");
        checkifUpdateQuery.setParameter("paraOwnerFirstName", "Jim");
        checkifUpdateQuery.setParameter("paraOwnerLastName", "Green");
        Phone phone = (Phone) checkifUpdateQuery.getSingleResult();

        assertTrue(rowsUpdated == 1);
        assertEquals("519", phone.getAreaCode());
        em.close();
    }
    
    /**
     * delete a phone
     */
    @Test
    public void test_Phone_4_delete() {
        EntityManager em = emf.createEntityManager();
        
        Query selecttargetOwnerQuery = em.createQuery("SELECT phone.id FROM Phone phone "
                                                    + "JOIN phone.owner o "
                                                    + "WHERE o.firstName = :paraFirstName "
                                                    + "AND o.lastName = :paraLastName");
        /*both queries are working*/
        //Query selecttargetOwnerQuery = em.createQuery("SELECT phone.id From Phone phone Where phone.owner.firstName = :paraFirstName And " + "phone.owner.lastName = :paraLastName");
        
        selecttargetOwnerQuery.setParameter("paraFirstName", "Jim");
        selecttargetOwnerQuery.setParameter("paraLastName", "Green");
        List<Integer> phonesIds = selecttargetOwnerQuery.getResultList();

        em.getTransaction().begin();
        Query deleteQuery = em.createQuery("DELETE FROM Phone phone WHERE phone.id = :paraPhoneId");
        deleteQuery.setParameter("paraPhoneId", phonesIds.get(0));
        int rowaffected = deleteQuery.executeUpdate();
        em.getTransaction().commit();
        
        Phone targetPhone = em.find(Phone.class, phonesIds.get(0));
        
        assertTrue(rowaffected ==1);
        assertNull(targetPhone);
        em.close();

    }
    
    /**
     * can create multiple records
     */
    @Test
    public void test_Phone_5_Creating_multiple_records() {
        
        EntityManager em = emf.createEntityManager();
        
        em.getTransaction().begin();
        Employee employeeOne = new Employee();
        employeeOne.setFirstName("Sam");
        employeeOne.setLastName("King");
        employeeOne.setSalary(1500);
        
        Employee employeeTwo = new Employee();
        employeeTwo.setFirstName("Ray");
        employeeTwo.setLastName("Allen");
        employeeTwo.setSalary(1500);
        
        em.persist(employeeOne);
        em.persist(employeeTwo);
        
        for(int i=1; i<6; i++) {
            Phone phoneNew = new Phone();
            phoneNew.setAreaCode("613");
            phoneNew.setPhoneNumber("000000"+i);
            phoneNew.setType("Cellphone");
            if(i%2==0) {
                phoneNew.setOwner(employeeOne);
            }else {
                phoneNew.setOwner(employeeTwo);
            }
            em.persist(phoneNew);
        }
        
        em.getTransaction().commit();
        
        Query checkIfPhonesCreateQuery = em.createQuery("SELECT COUNT(phone) FROM Phone phone ");
        Long totalPhonesCreated = (Long) checkIfPhonesCreateQuery.getSingleResult();
        assertTrue(totalPhonesCreated==5);
        
        Query checkIfEmployeesCreateQuery = em.createQuery("SELECT COUNT(emp) FROM Employee emp ");
        Long totalEmployeeCreated = (Long) checkIfEmployeesCreateQuery.getSingleResult();

        assertTrue(totalEmployeeCreated==3);
        
        em.close();
    }
    
    /**
     * find multiple phones of an employee
     */
    @Test
    public void test_Phone_6_Find_All_Phones_Relate_To_Employee() {
        
        EntityManager em = emf.createEntityManager();
        
        Query selectEmployeOnePhonesQuery = em.createQuery("SELECT phone From Phone phone JOIN phone.owner o WHERE o.firstName = :paraFirstName And o.lastName = :paraLastName");
        selectEmployeOnePhonesQuery.setParameter("paraFirstName", "Sam");
        selectEmployeOnePhonesQuery.setParameter("paraLastName", "King");
        List<Phone> phonesForOne = selectEmployeOnePhonesQuery.getResultList();
        
        Query selectEmployeTwoPhonesQuery = em.createQuery("SELECT phone From Phone phone JOIN phone.owner o WHERE o.firstName = :paraFirstName And o.lastName = :paraLastName");
        selectEmployeTwoPhonesQuery.setParameter("paraFirstName", "Ray");
        selectEmployeTwoPhonesQuery.setParameter("paraLastName", "Allen");
        List<Phone> phonesForTwo = selectEmployeTwoPhonesQuery.getResultList();
        
        assertTrue(phonesForOne.size()==2);
        assertTrue(phonesForTwo.size()==3);
        
        assertEquals("Sam", phonesForOne.get(0).owner.getFirstName());
        assertEquals("King", phonesForOne.get(0).owner.getLastName());
        assertEquals("Sam", phonesForOne.get(1).owner.getFirstName());
        assertEquals("King", phonesForOne.get(1).owner.getLastName());
        assertEquals("Ray", phonesForTwo.get(0).owner.getFirstName());
        assertEquals("Allen", phonesForTwo.get(0).owner.getLastName());
        assertEquals("Ray", phonesForTwo.get(1).owner.getFirstName());
        assertEquals("Allen", phonesForTwo.get(1).owner.getLastName());
        assertEquals("Ray", phonesForTwo.get(2).owner.getFirstName());
        assertEquals("Allen", phonesForTwo.get(2).owner.getLastName());
      
        em.close();
    }
    
    /**
     * find employee of a phone
     */
    @Test
    public void test_Phone_7_CriteriaBuilder_Find_Owner_Phones() {
        
        EntityManager em = emf.createEntityManager();
        
        CriteriaBuilder cb = em.getCriteriaBuilder(); 
        CriteriaQuery<Phone> cq = cb.createQuery(Phone.class);
        
        Root<Employee> employee = cq.from(Employee.class);
        Join<Employee,Phone> phones = employee.join("phones");
        
        Path<String> targetEmployeeFirstName = employee.get(Employee_.firstName);
        Path<String> targetEmployeeLastName = employee.get(Employee_.lastName);
        

        Predicate condition = cb.and(cb.equal(targetEmployeeFirstName, "Sam"), cb.equal(targetEmployeeLastName, "King"));

        cq.select(phones).where(condition);
        
        TypedQuery<Phone> typedQuery = em.createQuery(cq);
        List<Phone> result = typedQuery.getResultList();
        

        assertTrue(result.size() ==2);
        assertEquals("Sam", result.get(0).getOwner().getFirstName());
        assertEquals("King", result.get(1).getOwner().getLastName());
        
        em.close();
    }
    
    /**
     * upodate owner of the phone
     */
    @Test
    public void test_Phone_8_CriteriaBuilder_Update_Owner_Phone() {
        
        EntityManager em = emf.createEntityManager();
        
        CriteriaBuilder cb = em.getCriteriaBuilder(); 
        
        CriteriaUpdate<Phone> cu= cb.createCriteriaUpdate(Phone.class);
        
        Root<Phone> phone = cu.from(Phone.class);
        
        cu.set("phoneNumber", "1231234");
        cu.where(cb.and(cb.equal(phone.get(Phone_.areaCode), "613"), cb.equal(phone.get(Phone_.phoneNumber), "0000002")));
        em.getTransaction().begin();
        Query query = em.createQuery(cu);
        int rowAffect = query.executeUpdate();
        
        em.getTransaction().commit();
        
        assertTrue(rowAffect==1);
        
        em.close();
    }
    
    /**
     * delete owner phone
     */
    @Test
    public void test_Phone_9_CriteriaBuilder_Delete_Owner_Phone() {
        
        EntityManager em = emf.createEntityManager();
        
        CriteriaBuilder cb = em.getCriteriaBuilder(); 
        
        CriteriaDelete<Phone> cd = cb.createCriteriaDelete(Phone.class);
        
        Root<Phone> phone = cd.from(Phone.class);
        
        Predicate condition = cb.and(cb.equal(phone.get(Phone_.areaCode), "613"), cb.equal(phone.get(Phone_.phoneNumber), "0000004"));
        
        em.getTransaction().begin();
        cd.where(condition);
        Query query = em.createQuery(cd);
        int rowAffect = query.executeUpdate();
        em.getTransaction().commit();
        
        assertTrue(rowAffect==1);
        
        em.close();
        
    }
    
    /**
     * find phone using like string
     */
    @Test
    public void test_Phone_a_CriteriaBuilder_Like_String() {
        
        EntityManager em = emf.createEntityManager();
        
        CriteriaBuilder cb = em.getCriteriaBuilder();
        
        CriteriaQuery<Phone> cq = cb.createQuery(Phone.class);
        
        Root<Employee> employee = cq.from(Employee.class);
        Join<Employee,Phone> phones = employee.join("phones");
        
        Expression<String> path = employee.get(Employee_.firstName);
        
        Predicate l1 = cb.like(path, "Sa%");
        
        cq.select(phones).where(l1);
        
        TypedQuery<Phone> typedQuery = em.createQuery(cq);
        List<Phone> result = typedQuery.getResultList();
        
        assertTrue(result.size() ==1);
        assertEquals("Sam", result.get(0).getOwner().getFirstName());
        
        em.close();
    }
    
    /**
     * find phone using concat columns
     */
    @Test
    public void test_Phone_b_CriteriaBuilder_concat_columns() {
        
        EntityManager em = emf.createEntityManager();
        
        CriteriaBuilder cb = em.getCriteriaBuilder();
        
        CriteriaQuery<Phone> cq = cb.createQuery(Phone.class);
        
        Root<Employee> employee = cq.from(Employee.class);
        Join<Employee,Phone> phones = employee.join("phones");
        
        Expression<String> concatExp = cb.concat(employee.get(Employee_.firstName), " ");
        concatExp = cb.concat(concatExp, employee.get(Employee_.lastName));
        
        Predicate condition =  cb.like(concatExp, "Ray Allen");
        
        cq.select(phones).where(condition);
        
        TypedQuery<Phone> typedQuery = em.createQuery(cq);
        List<Phone> result = typedQuery.getResultList();

        assertTrue(result.size() == 3);
        
        for(int i=0; i<3; i++) {
            assertEquals("Ray", result.get(i).getOwner().getFirstName());
            assertEquals("Allen", result.get(i).getOwner().getLastName());
        }
        
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
