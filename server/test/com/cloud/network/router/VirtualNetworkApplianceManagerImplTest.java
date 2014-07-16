package com.cloud.network.router;

import static org.mockito.Mockito.when;

import java.util.LinkedHashMap;
import java.util.List;

import org.apache.cloudstack.framework.config.dao.ConfigurationDao;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.cloud.cluster.dao.ManagementServerHostDao;
import com.cloud.dc.DataCenter;
import com.cloud.dc.DataCenter.NetworkType;
import com.cloud.dc.DataCenterVO;
import com.cloud.dc.dao.ClusterDao;
import com.cloud.dc.dao.DataCenterDao;
import com.cloud.dc.dao.HostPodDao;
import com.cloud.dc.dao.VlanDao;
import com.cloud.deploy.DataCenterDeployment;
import com.cloud.deploy.DeployDestination;
import com.cloud.deploy.DeploymentPlan;
import com.cloud.host.dao.HostDao;
import com.cloud.hypervisor.Hypervisor.HypervisorType;
import com.cloud.network.Network;
import com.cloud.network.dao.FirewallRulesDao;
import com.cloud.network.dao.IPAddressDao;
import com.cloud.network.dao.LoadBalancerDao;
import com.cloud.network.dao.LoadBalancerVMMapDao;
import com.cloud.network.dao.MonitoringServiceDao;
import com.cloud.network.dao.NetworkDao;
import com.cloud.network.dao.OpRouterMonitorServiceDao;
import com.cloud.network.dao.PhysicalNetworkServiceProviderDao;
import com.cloud.network.dao.RemoteAccessVpnDao;
import com.cloud.network.dao.Site2SiteCustomerGatewayDao;
import com.cloud.network.dao.Site2SiteVpnConnectionDao;
import com.cloud.network.dao.Site2SiteVpnGatewayDao;
import com.cloud.network.dao.UserIpv6AddressDao;
import com.cloud.network.dao.VirtualRouterProviderDao;
import com.cloud.network.dao.VpnUserDao;
import com.cloud.network.element.VirtualRouterProviderVO;
import com.cloud.network.router.VirtualRouter.RedundantState;
import com.cloud.network.rules.dao.PortForwardingRulesDao;
import com.cloud.offerings.dao.NetworkOfferingDao;
import com.cloud.resource.ResourceManager;
import com.cloud.service.dao.ServiceOfferingDao;
import com.cloud.storage.dao.GuestOSDao;
import com.cloud.storage.dao.VMTemplateDao;
import com.cloud.storage.dao.VolumeDao;
import com.cloud.user.AccountManager;
import com.cloud.user.AccountVO;
import com.cloud.user.dao.UserDao;
import com.cloud.user.dao.UserStatisticsDao;
import com.cloud.user.dao.UserStatsLogDao;
import com.cloud.vm.DomainRouterVO;
import com.cloud.vm.NicProfile;
import com.cloud.vm.VirtualMachineManager;
import com.cloud.vm.dao.DomainRouterDao;
import com.cloud.vm.dao.NicDao;
import com.cloud.vm.dao.NicIpAliasDao;
import com.cloud.vm.dao.UserVmDao;
import com.cloud.vm.dao.UserVmDetailsDao;
import com.cloud.vm.dao.VMInstanceDao;

@RunWith(MockitoJUnitRunner.class)
public class VirtualNetworkApplianceManagerImplTest {
    @Mock
    private ClusterDao _clusterDao;

    @Mock
    private ConfigurationDao _configDao;

    @Mock
    private DataCenterDao _dcDao;

    @Mock
    private GuestOSDao _guestOSDao;

    @Mock
    private HostDao _hostDao;

    @Mock
    private IPAddressDao _ipAddressDao;

    @Mock
    private UserIpv6AddressDao _ipv6Dao;

    @Mock
    private LoadBalancerDao _loadBalancerDao;

    @Mock
    private LoadBalancerVMMapDao _loadBalancerVMMapDao;

    @Mock
    private MonitoringServiceDao _monitorServiceDao;

    @Mock
    private ManagementServerHostDao _msHostDao;

    @Mock
    private NetworkDao _networkDao;

    @Mock
    private NetworkOfferingDao _networkOfferingDao;

    @Mock
    private NicDao _nicDao;

    @Mock
    private NicIpAliasDao _nicIpAliasDao;

    @Mock
    private OpRouterMonitorServiceDao _opRouterMonitorServiceDao;

    @Mock
    private PortForwardingRulesDao _pfRulesDao;

    @Mock
    private PhysicalNetworkServiceProviderDao _physicalProviderDao;

    @Mock
    private HostPodDao _podDao;

    @Mock
    private DomainRouterDao _routerDao;

    @Mock
    private FirewallRulesDao _rulesDao;

    @Mock
    private Site2SiteCustomerGatewayDao _s2sCustomerGatewayDao;

    @Mock
    private Site2SiteVpnConnectionDao _s2sVpnConnectionDao;

    @Mock
    private Site2SiteVpnGatewayDao _s2sVpnGatewayDao;

    @Mock
    private ServiceOfferingDao _serviceOfferingDao;

    @Mock
    private VMTemplateDao _templateDao;

    @Mock
    private UserDao _userDao;

    @Mock
    private UserStatisticsDao _userStatsDao;

    @Mock
    private UserStatsLogDao _userStatsLogDao;

    @Mock
    private UserVmDao _userVmDao;

    @Mock
    private VlanDao _vlanDao;

    @Mock
    private VMInstanceDao _vmDao;

    @Mock
    private UserVmDetailsDao _vmDetailsDao;

    @Mock
    private VolumeDao _volumeDao;

    @Mock
    private RemoteAccessVpnDao _vpnDao;

    @Mock
    private VpnUserDao _vpnUsersDao;

    @Mock
    private VirtualRouterProviderDao _vrProviderDao;

    @Mock private AccountManager _accountMgr;

    @Mock private VirtualMachineManager _itMgr;

    @Mock private ResourceManager _resourceMgr;

    @InjectMocks
    private VirtualNetworkApplianceManagerImpl virtualNetworkApplianceManagerImpl;

    @Test
    public void testDestroyRouter() throws Exception {
        VirtualRouter r = new DomainRouterVO(1L, 0L, 0L, "router", 0L, HypervisorType.Any, 0L, 0L,
                1L, false, 0, false, RedundantState.UNKNOWN, false, false, null);
        when(_routerDao.findById(1L)).thenReturn((DomainRouterVO)r);
        VirtualRouter vr = virtualNetworkApplianceManagerImpl.destroyRouter(1L, new AccountVO(1L), 0L);
        assert vr != null;
    }

    @Test
    public void testDeployRouterNotRedundant() throws Exception {
        DataCenter dc = new DataCenterVO(1L, "name", "description", "dns", null, null, null, "cidr", "domain", null,
                NetworkType.Basic, "zoneToken", "domainSuffix");
        when(_routerDao.getNextInSequence(Long.class, "id")).thenReturn(1L);
        when(_resourceMgr.getDefaultHypervisor(1L)).thenReturn(HypervisorType.Any);
        DeploymentPlan plan = new DataCenterDeployment(1L);
        VirtualRouter vr = virtualNetworkApplianceManagerImpl.deployRouter(new AccountVO(1L), new DeployDestination(dc,null,null,null), plan, null, false,
                new VirtualRouterProviderVO(), 0L, null, new LinkedHashMap<Network, List<? extends NicProfile>> (), true /* start the router */,
                null);
        assert vr != null;
    }
}
