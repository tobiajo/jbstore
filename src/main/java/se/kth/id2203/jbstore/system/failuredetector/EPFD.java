package se.kth.id2203.jbstore.system.failuredetector;

import se.kth.id2203.jbstore.system.network.NodePort;
import se.kth.id2203.jbstore.system.failuredetector.event.EPFDInit;
import se.kth.id2203.jbstore.system.failuredetector.event.EPFDRestore;
import se.kth.id2203.jbstore.system.failuredetector.event.EPFDSuspect;
import se.kth.id2203.jbstore.system.network.event.NodeMsg;
import se.kth.id2203.jbstore.system.network.event.NodeMsgSend;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.kompics.Positive;
import se.sics.kompics.timer.ScheduleTimeout;
import se.sics.kompics.timer.Timeout;
import se.sics.kompics.timer.Timer;
import se.sics.test.TAddress;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class EPFD extends ComponentDefinition {

    private final Positive<Timer> timerPositive = requires(Timer.class);
    private final Positive<NodePort> nodePortPositive = requires(NodePort.class);
    private final Positive<EPFDPort> epfdPortPositive = requires(EPFDPort.class);
    private final Random rnd = new Random();

    private EventuallyPerfectFailureDetector diamondP;

    private Handler<EPFDInit> epfdInitHandler = new Handler<EPFDInit>() {
        @Override
        public void handle(EPFDInit event) {
            diamondP = new EventuallyPerfectFailureDetector(event.nodesToMonitor);
        }
    };

    private Handler<NodeMsg> netMsgHandler = new Handler<NodeMsg>() {
        @Override
        public void handle(NodeMsg nodeMsg) {
            if (diamondP == null) {
                return;
            }
            if (rnd.nextBoolean()) {
                switch (nodeMsg.cmd) {
                    case NodeMsg.HEARTBEAT_REQUEST:
                        diamondP.heartbeatRequest(nodeMsg);
                        break;
                    case NodeMsg.HEARTBEAT_REPLY:
                        diamondP.heartbeatReply(nodeMsg);
                        break;
                }
            }
        }
    };

    private Handler<HeartbeatTimeout> heartbeatTimeoutHandler = new Handler<HeartbeatTimeout>() {
        @Override
        public void handle(HeartbeatTimeout heartbeatTimeout) {
            diamondP.timeout();
        }
    };

    {
        subscribe(epfdInitHandler, epfdPortPositive);
        subscribe(netMsgHandler, epfdPortPositive);
        subscribe(heartbeatTimeoutHandler, timerPositive);
    }

    private void send(TAddress dst, long rid, byte cmd, Serializable body) {
        trigger(new NodeMsgSend(dst, rid, NodeMsg.EPFD, cmd, -1, body), nodePortPositive);
    }

    private void send(Set<TAddress> dstGroup, long rid, byte cmd, Serializable body) {
        for (TAddress dst : dstGroup) {
            send(dst, rid, cmd, body);
        }
    }

    private void startTimer(long delay) {
        ScheduleTimeout st = new ScheduleTimeout(delay);
        st.setTimeoutEvent(new HeartbeatTimeout(st));
        trigger(st, timerPositive);
    }

    // Algorithm 2.7: Increasing Timeout
    private class EventuallyPerfectFailureDetector {

        static final long DELTA = 1000;
        int rid = 0;
        final HashSet<TAddress> nodesToMonitor;

        HashSet<TAddress> alive;
        HashSet<TAddress> suspected;
        long delay;

        EventuallyPerfectFailureDetector(HashSet<TAddress> nodesToMonitor) {
            this.nodesToMonitor = nodesToMonitor;
            diamondP.init();
        }

        void init() {
            alive = new HashSet<>(nodesToMonitor);
            suspected = new HashSet<>();
            delay = DELTA;
            startTimer(delay);
        }

        void timeout() {
            if (getAliveSuspectIntersection().size() != 0) {
                delay = delay + DELTA;
            }
            for (TAddress p : nodesToMonitor) {
                if (!alive.contains(p) && !suspected.contains(p)) {
                    suspected.add(p);
                    trigger(new EPFDSuspect(p), epfdPortPositive);
                } else if (alive.contains(p) && suspected.contains(p)) {
                    suspected.remove(p);
                    trigger(new EPFDRestore(p), epfdPortPositive);
                }
                send(p, ++rid, NodeMsg.HEARTBEAT_REQUEST, null);
            }
            alive = new HashSet<>();
            startTimer(delay);
        }

        HashSet<TAddress> getAliveSuspectIntersection() {
            HashSet<TAddress> intersection = new HashSet<>(alive);
            intersection.retainAll(suspected);
            return intersection;
        }

        void heartbeatRequest(NodeMsg nodeMsg) {
            send(nodeMsg.getSource(), nodeMsg.rid, NodeMsg.HEARTBEAT_REPLY, null);
        }

        void heartbeatReply(NodeMsg nodeMsg) {
            alive.add(nodeMsg.getSource());
        }
    }

    private class HeartbeatTimeout extends Timeout {

        HeartbeatTimeout(ScheduleTimeout request) {
            super(request);
        }
    }
}
