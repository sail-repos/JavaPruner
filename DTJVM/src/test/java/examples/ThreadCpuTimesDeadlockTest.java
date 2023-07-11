//package examples;
//
//import org.junit.Test;
//
//import javax.management.JMException;
//import javax.management.MBeanServer;
//import javax.management.MalformedObjectNameException;
//import javax.management.ObjectName;
//import java.lang.management.ManagementFactory;
//
//public class ThreadCpuTimesDeadlockTest {
//
//    private static final String HOTSPOT_INTERNAL = "sun.management:type=HotspotInternal";
//
//    @Test
//    public void test(){
//
//        MBeanServer server = ManagementFactory.getPlatformMBeanServer();
//        ObjectName objName= null;
//        try {
//            ObjectName hotspotInternal = new ObjectName(HOTSPOT_INTERNAL);
//            try {
//                server.registerMBean(new sun.management.HotspotInternal(), hotspotInternal);
//            } catch (JMException e) {
//                throw new RuntimeException("HotSpotWatcher: Failed to register the HotspotInternal MBean" + e);
//            }
//            objName= new ObjectName("sun.management:type=HotspotThreading");
//
//        } catch (MalformedObjectNameException e1) {
//            throw new RuntimeException("Bad object name" + e1);
//        }
//    }
//}
