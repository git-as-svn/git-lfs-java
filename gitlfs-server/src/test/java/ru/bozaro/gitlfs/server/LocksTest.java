package ru.bozaro.gitlfs.server;

import org.testng.Assert;
import org.testng.annotations.Test;
import ru.bozaro.gitlfs.client.Client;
import ru.bozaro.gitlfs.client.auth.AuthProvider;
import ru.bozaro.gitlfs.common.LockConflictException;
import ru.bozaro.gitlfs.common.VerifyLocksResult;
import ru.bozaro.gitlfs.common.data.Lock;
import ru.bozaro.gitlfs.common.data.Ref;

import java.util.List;

public final class LocksTest {
  @Test
  public void simple() throws Exception {
    final MemoryStorage storage = new MemoryStorage(-1);
    try (final EmbeddedLfsServer server = new EmbeddedLfsServer(storage, new MemoryLockManager(storage))) {
      final AuthProvider auth = server.getAuthProvider();
      final Client client = new Client(auth);

      final Ref ref = Ref.create("ref/heads/master");

      final Lock lock = client.lock("qwe", ref);
      Assert.assertNotNull(lock);

      try {
        client.lock("qwe", ref);
        Assert.fail();
      } catch (LockConflictException e) {
        Assert.assertEquals(lock.getId(), e.getLock().getId());
      }

      {
        final List<Lock> locks = client.listLocks("qwe", null, ref);
        Assert.assertEquals(locks.size(), 1);
        Assert.assertEquals(locks.get(0).getId(), lock.getId());
      }

      {
        final VerifyLocksResult locks = client.verifyLocks(ref);
        Assert.assertEquals(locks.getOurLocks().size(), 1);
        Assert.assertEquals(locks.getOurLocks().get(0).getId(), lock.getId());
        Assert.assertEquals(locks.getTheirLocks().size(), 0);
      }

      final Lock unlock = client.unlock(lock.getId(), true, ref);
      Assert.assertNotNull(unlock);
      Assert.assertEquals(unlock.getId(), lock.getId());

      Assert.assertNull(client.unlock(lock.getId(), false, ref));
    }
  }
}
