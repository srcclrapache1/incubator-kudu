/*
 * Copyright (c) 2014 Cloudera, Inc.
 * Confidential Cloudera Information: Covered by NDA.
 */
package kudu.rpc;

import java.util.List;

import com.google.common.collect.Lists;
import com.google.common.collect.ImmutableList;
import kudu.master.Master.TabletLocationsPB;
import kudu.master.Master.TabletLocationsPB.ReplicaPB;
import kudu.Common.HostPortPB;
import kudu.consensus.Metadata.QuorumPeerPB.Role;

/**
 * Information about the locations of tablets in a Kudu table.
 * This should be treated as immutable data (it does not reflect
 * any updates the client may have heard since being constructed).
 */
public class LocatedTablet {
  private final byte[] startKey;
  private final byte[] endKey;
  private final byte[] tabletId;

  private final List<Replica> replicas;

  LocatedTablet(TabletLocationsPB pb) {
    this.startKey = pb.getStartKey().toByteArray();
    this.endKey = pb.getEndKey().toByteArray();
    this.tabletId = pb.getTabletId().toByteArray();

    List<Replica> reps = Lists.newArrayList();
    for (ReplicaPB repPb : pb.getReplicasList()) {
      reps.add(new Replica(repPb));
    }
    this.replicas = ImmutableList.copyOf(reps);
  }

  public List<Replica> getReplicas() {
    return replicas;
  }

  public byte[] getStartKey() {
    return startKey;
  }

  public byte[] getEndKey() {
    return endKey;
  }

  public byte[] getTabletId() {
    return tabletId;
  }

  /**
   * Return the current leader, or null if there is none.
   */
  public Replica getLeaderReplica() {
    return getOneOfRoleOrNull(Role.LEADER);
  }

  /**
   * Return the first occurrence for the given role, or null if there is none.
   */
  private Replica getOneOfRoleOrNull(Role role) {
    for (Replica r : replicas) {
      if (r.getRole() == role) return r;
    }
    return null;
  }

  @Override
  public String toString() {
    return Bytes.pretty(tabletId)
      + "[" + Bytes.pretty(startKey) + ","
      + Bytes.pretty(endKey) + "]";
  }

  /**
   * One of the replicas of the tablet.
   */
  public static class Replica {
    private final ReplicaPB pb;

    private Replica(ReplicaPB pb) {
      this.pb = pb;
    }

    public HostPortPB getRpcHostPort() {
      if (pb.getTsInfo().getRpcAddressesList().isEmpty()) {
        return null;
      }
      return pb.getTsInfo().getRpcAddressesList().get(0);
    }

    public Role getRole() {
      return pb.getRole();
    }

    public String toString() {
      return pb.toString();
    }
  }

};
