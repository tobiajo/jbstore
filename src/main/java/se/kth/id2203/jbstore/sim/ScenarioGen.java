package se.kth.id2203.jbstore.sim;

import se.kth.id2203.jbstore.system.ClientParent;
import se.kth.id2203.jbstore.system.NodeParent;
import se.sics.kompics.Init;
import se.sics.kompics.network.Address;
import se.sics.kompics.simulator.SimulationScenario;
import se.sics.kompics.simulator.adaptor.Operation1;
import se.sics.kompics.simulator.adaptor.Operation2;
import se.sics.kompics.simulator.adaptor.distributions.ConstantDistribution;
import se.sics.kompics.simulator.adaptor.distributions.extra.BasicIntSequentialDistribution;
import se.sics.kompics.simulator.events.system.StartNodeEvent;
import se.sics.test.TAddress;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class ScenarioGen {

    public static final String IP_NET = "240.0.0.";
    public static final int PORT = 0;
    public static final int NODES = 3;

    static Operation1 startCreatorOp = new Operation1<StartNodeEvent, Integer>() {

        @Override
        public StartNodeEvent generate(final Integer self) {
            return new StartNodeEvent() {
                TAddress selfAdr;

                {
                    try {
                        selfAdr = new TAddress(InetAddress.getByName(IP_NET + self), PORT);
                    } catch (UnknownHostException ex) {
                        throw new RuntimeException(ex);
                    }
                }

                @Override
                public Address getNodeAddress() {
                    return selfAdr;
                }

                @Override
                public Class getComponentDefinition() {
                    return NodeParent.class;
                }

                @Override
                public Init getComponentInit() {
                    return new NodeParent.Init(selfAdr, null, self, NODES);
                }

                @Override
                public String toString() {
                    return "StartCreator<" + selfAdr.toString() + ">";
                }
            };
        }
    };

    static Operation2 startJoinerOp = new Operation2<StartNodeEvent, Integer, Integer>() {

        @Override
        public StartNodeEvent generate(final Integer self, final Integer member) {
            return new StartNodeEvent() {
                TAddress selfAdr;
                TAddress memberAdr;

                {
                    try {
                        selfAdr = new TAddress(InetAddress.getByName(IP_NET + self), PORT);
                        memberAdr = new TAddress(InetAddress.getByName(IP_NET + member), PORT);
                    } catch (UnknownHostException ex) {
                        throw new RuntimeException(ex);
                    }
                }

                @Override
                public Address getNodeAddress() {
                    return selfAdr;
                }

                @Override
                public Class getComponentDefinition() {
                    return NodeParent.class;
                }

                @Override
                public Init getComponentInit() {
                    return new NodeParent.Init(selfAdr, memberAdr, self, NODES);
                }

                @Override
                public String toString() {
                    return "StartJoiner<" + selfAdr.toString() + ">";
                }
            };
        }
    };

    static Operation2 startClientOp = new Operation2<StartNodeEvent, Integer, Integer>() {

        @Override
        public StartNodeEvent generate(final Integer self, final Integer member) {
            return new StartNodeEvent() {
                TAddress selfAdr;
                TAddress memberAdr;

                {
                    try {
                        selfAdr = new TAddress(InetAddress.getByName(IP_NET + self), PORT);
                        memberAdr = new TAddress(InetAddress.getByName(IP_NET + member), PORT);
                    } catch (UnknownHostException ex) {
                        throw new RuntimeException(ex);
                    }
                }

                @Override
                public Address getNodeAddress() {
                    return selfAdr;
                }

                @Override
                public Class getComponentDefinition() {
                    return ClientParent.class;
                }

                @Override
                public Init getComponentInit() {
                    return new ClientParent.Init(selfAdr, memberAdr);
                }

                @Override
                public String toString() {
                    return "StartClient<" + selfAdr.toString() + ">";
                }
            };
        }
    };

    public static SimulationScenario simpleCluster() {
        SimulationScenario scen = new SimulationScenario() {
            {
                ConstantDistribution creatorIp = new ConstantDistribution(Integer.class, 1);
                BasicIntSequentialDistribution joinerIp = new BasicIntSequentialDistribution(2);

                StochasticProcess creator = new StochasticProcess() {
                    {
                        eventInterArrivalTime(constant(1000));
                        raise(1, startCreatorOp, creatorIp);
                    }
                };

                StochasticProcess joiner = new StochasticProcess() {
                    {
                        eventInterArrivalTime(constant(1000));
                        for (int i = 0; i < NODES - 1; i++) {
                            raise(1, startJoinerOp, joinerIp, creatorIp);
                        }
                    }
                };

                StochasticProcess client = new StochasticProcess() {
                    {
                        eventInterArrivalTime(constant(1000));
                        raise(1, startClientOp, joinerIp, creatorIp);

                    }
                };

                creator.start();
                joiner.startAfterTerminationOf(1000, creator);
                client.startAfterTerminationOf(1000, joiner);
                terminateAfterTerminationOf(10000, joiner);
            }
        };

        return scen;
    }
}
