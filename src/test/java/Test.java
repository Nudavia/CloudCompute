import org.dom4j.DocumentException;
import org.libvirt.LibvirtException;
import utils.LinuxConnection;

public class Test {
    @org.junit.Test
    public void test1() throws LibvirtException, DocumentException {
//        Connect connect2 = new Connect("qemu+tcp://192.168.43.129:16509/system");
//        StorageVol vol = connect2.storageVolLookupByPath("/opt/kvm/images/domain03_1648145560263");
//        System.out.println(vol.getInfo().allocation);
//        System.out.println(SystemManager.chooseNodeIP());
//        Connect connect = new Connect("qemu+tcp://192.168.43.130:16509/system");
//        VMService vmService = new VMService();

//        String domainXML = XMLUtils.createDomainXML("domain01", 2, 2, "/opt/kvm/images/image01.qcow2", "66:66:66:66:66:0a");
//        String volXML = XMLUtils.createVolXML("/opt/kvm/images/domain03_1648151215573", "domain03_1648151215573", 10);
//        StoragePool pool = connect.storagePoolLookupByName("pool01");
//        pool.storageVolCreateXML(volXML, 0);
//        connect.domainDefineXML(domainXML);
//        System.out.println(connect.getCapabilities());
//        System.out.println(connect.getHostName());
//        System.out.println(connect.getFreeMemory() / SystemManager.B2GB);
//        System.out.println(connect.getMaxVcpus("kvm"));
//        StoragePool pool = connect.storagePoolLookupByName("pool01");
//        System.out.println(pool.getInfo().allocation / SystemManager.B2GB);
//        System.out.println(pool.getInfo().capacity / SystemManager.B2GB);
//        System.out.println(pool.getInfo().available / SystemManager.B2GB);
//        Domain domain = connect.domainLookupByID(1);
//        connect.close();
//        domain.migrate(connect2, 0, null, "tcp://" + "192.168.43.128", 0);
//        PMManager pmm = PMManager.getPMM(connect);
//        String pool01 = XMLUtils.createPoolXML("pool01", 40, "/opt/kvm/images");
//        pmm.runStoragePool("pool01");
//        pmm.defineStoragePool(pool01);
//        String domainXML = XMLUtils.createDomainXML("domain01", RandomCreator.createUUID(),
//                2, 2, "/opt/kvm/images/image01.qcow2", RandomCreator.createMac());
//        vmm.defineDomain(domainXML);
//        vmm.runDomain("domain01");
//        VMManager vmm = VMManager.getVMM(connect);
//        Domain domain = vmm.getDomainByName("domain01");
//        System.out.println(domain.getInfo().maxMem);
//        System.out.println(domain.getInfo().memory);
//        System.out.println(domain.getInfo().maxMem);
//        System.out.println(domain.getInfo().maxMem);
//        VcpuInfo[] vcpusInfo = domain.getVcpusInfo();
//        for (VcpuInfo vcpuInfo : vcpusInfo) {
//            System.out.println(vcpuInfo.cpu);
//            System.out.println(vcpuInfo.cpuTime);
//            System.out.println(vcpuInfo.number);
//            System.out.println(vcpuInfo.state);
//        }
//        for (MemoryStatistic memoryStatistic : domain.memoryStats(1)) {
//
//            System.out.println(memoryStatistic.getTag());
//            System.out.println(memoryStatistic.getValue());
//        }
//
//
//        System.out.println(domain.getOSType());
//        System.out.println(domain.getInfo().nrVirtCpu);
//        System.out.println(domain.getInfo().state);
//        System.out.println(domain.getInfo().maxMem);
//        System.out.println(domain.getInfo().memory);
//        System.out.println(domain.getInfo().cpuTime);
//        System.out.println(domain.getMaxMemory());
//        System.out.println(domain.getMaxVcpus());
//        System.out.println(domain.getName());
//        System.out.println(domain.getID());
//        System.out.println(domain.getJobInfo().getDataProcessed());
//        System.out.println(domain.getJobInfo().getDataTotal());
//        System.out.println(domain.getJobInfo().getDataRemaining());
//
//        System.out.println(domain.getJobInfo().getFileProcessed());
//        System.out.println(domain.getJobInfo().getFileTotal());
//        System.out.println(domain.getJobInfo().getFileRemaining());
//
//        System.out.println(domain.getJobInfo().getMemProcessed());
//        System.out.println(domain.getJobInfo().getMemTotal());
//        System.out.println(domain.getJobInfo().getMemRemaining());
//
//
//        System.out.println(domain.getJobInfo().getTimeElapsed());
//        System.out.println(domain.getJobInfo().getTimeRemaining());
//
//        System.out.println(domain.getJobInfo().getType());
//
//        DomainBlockInfo domainBlockInfo = domain.blockInfo("opt/kvm/images/image01.qcow2");
//        System.out.println(domainBlockInfo.getCapacity());
//        System.out.println(domainBlockInfo.getAllocation());
//        System.out.println(domainBlockInfo.getPhysical());


//        vmm.deleteStorageVol("pool01", "image01.qcow2");
//        vmm.defineDomain("src/main/resources/domain.xml");
//        vmm.deleteStorageVol("pool01", "image01.qcow2");
//        vmm.deleteStoragePool("pool01");
//        vmm.deleteStorageVol("pool01", "image01");
//        vmm.getStorageVolByName("pool01", "image01");
//        vmm.createStorageVol("src/main/resources/storage-volume.xml", "pool01");
//        vmm.deleteStorageVol("pool01","image01");
    }

    @org.junit.Test
    public void test2() {
//        String volPath = "opt/kvm/images";
//        String domainName = "test";
//        String volName = domainName + "_" + System.currentTimeMillis();
//        XMLCreator.createVolXML(volPath, volName, 10);
//        XMLCreator.createDomainXML(domainName, RandomCreator.createUUID(), 4, 4,
//                volPath + "/" + volName, RandomCreator.createMac());
    }

    @org.junit.Test
    public void Test3() {
//        VMService vmService = new VMService();
//        for (VM vm : vmService.listVM()) {
//            System.out.println(vm);
//        }
    }

    @org.junit.Test
    public void Test4() {
//        ZKClient.createIP4PM("test", "123");
//        ZKClient.createIP4VM("test", "domain", "1234");
//        System.out.println(ZKClient.getIP4PM("ldw01"));
//        System.out.println(new String(ZKClient.getIP4VM("ldw01", "domain01")));
//        Map<String, String> stringStringMap = ZKClient.listIP4PM();
//        System.out.println(stringStringMap);
//        ZKClient.deleteIP4VM("ldw01", "domain02");
//        Map<String, String> stringStringMap1 = ZKClient.listIP4VM();
//        ZKClient.deleteIP4PM("test");
//        ZKClient.deleteIP4VM("test", "domain");
    }

    @org.junit.Test
    public void Test5() {
//        XMLUtils.createVolXML("/opt/kvm/images/domain01_1647795713252",
//                "domain01_1647795713252",
//                10);
    }


    @org.junit.Test
    public void Test6() throws Exception {
        String linuxIP = "192.168.43.130";
        String usrName = "root";
        String passwd = "ldw20000702";
        LinuxConnection linuxConnection = new LinuxConnection(linuxIP, usrName, passwd);
        String commend = "docker inspect " + "mpi02" + " |grep IPAddress|tail -1";
        String result = linuxConnection.execute(commend);

        String[] s = result.trim().split("[:\",\\s+]");

        System.out.println(s[s.length - 1]);
    }

    @org.junit.Test
    public void Test7() throws Exception {
//        VMService vmService = new VMService();
//        while (true) {
//            vmService.getStatistic("domain01");
//        }
    }
}


