package org.example.agent.internal;

import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;
import com.sun.tools.attach.spi.AttachProvider;

import java.util.List;

public class DummyAttachProvider extends AttachProvider {

    @Override
    public VirtualMachine attachVirtualMachine(final String arg0) {
        return null;
    }

    @Override
    public List<VirtualMachineDescriptor> listVirtualMachines() {
        return null;
    }

    @Override
    public String name() {
        return null;
    }

    @Override
    public String type() {
        return null;
    }

}
