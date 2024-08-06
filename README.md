You need to define three operations on it

1. lock(X, uid)

2 unlock(x, uid)

3 upgradeLock(x, uid)

where X the name of a node in the tree (that would be unique) and uid is the user who is performing the operation.

Here are the definitions for the Operations:

Lock(x, uid)

Lock takes an exclusive access on the subtree rooted at X. It is formally defined like this. Once lock(x, uid) succeeds, then

lock(A, anyUsend) should fail (returns false), where A is a descendent of X.

lock(B, anyUserld) should fail (returns false), where X is a descendent of B

Lock operation cannot be performed on a node which is already locked ie lock(x, anyUserld) should fail (returns false)

Unlock(X, uid)

Unlock reverts what was done by the lock operation. It can only be called on same node on which user uid had called a Lock on before. Returns true if it is successful

Upgradel.ock(x, uid)

It helps the user uid upgrade their lock to an ancestor node It is only possible if the node X already has locked descendants and all of them are only locked by the same user uid. Upgrade should fail if there is any node which is descendant of X that is locked by a different user. Successful Upgrade will 'lock' the node UpgradeLock call shouldn't violate the consistency model that Lock/Unlock function requires

Notes

1) The riumber of nodes in the tree N is very large. So, optimize the time complexity for the above algorithms

2) The below section contains the input format The first line contains the number of Nodes in the tree (N)
3) The second line contains number of children per node (value m in m-ary Tree)

The third line contains number of queries (Q)

Next N lines contains the NodeName (string) in the m-Ary Tree.

Next Q lines contains queries which are in format: Operation Type NodeName Userld

Operation Type-

1 for Lock

2 for unlock

3 for upgradeLock

• NodeName - Name of any node (unique) in m-Ary Tree

Userld - Integer value representing a unique user.
