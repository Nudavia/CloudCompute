package entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import manager.SystemManager;

/**
 * @Auther: ldw
 * @Date: 2022/3/29
 */
@Data
@NoArgsConstructor
public class VMInfo {
    private int nVcpu;
    private double cpuRate;

    private double memRate;
    private double memTotal;

    private double diskAllocation;
    private double diskTotal;

    public VMInfo(int nVcpu, double cpuRate, double diskAllocation, double diskTotal, double memTotal, double memAvailable) {

        this.nVcpu = nVcpu;
        this.cpuRate = Double.parseDouble(SystemManager.round4.format(cpuRate));
        this.diskAllocation = Double.parseDouble(SystemManager.round2.format(diskAllocation));
        this.diskTotal = Double.parseDouble(SystemManager.round2.format(diskTotal));
        this.memTotal = Double.parseDouble(SystemManager.round2.format(memTotal));
        this.memRate = Double.parseDouble(SystemManager.round2.format(1 - memAvailable / memTotal));
    }

    @Override
    public String toString() {
        return "VMInfo{" +
                "nVcpu=" + nVcpu +
                ", cpuRate=" + cpuRate +
                ", memRate=" + memRate +
                ", memTotal=" + memTotal +
                ", diskAllocation=" + diskAllocation +
                ", diskTotal=" + diskTotal +
                '}';
    }
}
