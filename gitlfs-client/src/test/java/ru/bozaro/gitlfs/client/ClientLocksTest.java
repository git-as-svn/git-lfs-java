package ru.bozaro.gitlfs.client;

import org.testng.Assert;
import org.testng.annotations.Test;
import ru.bozaro.gitlfs.common.LockConflictException;
import ru.bozaro.gitlfs.common.VerifyLocksResult;
import ru.bozaro.gitlfs.common.data.Lock;
import ru.bozaro.gitlfs.common.data.Ref;

import java.io.IOException;
import java.util.List;

public final class ClientLocksTest {

  @Test
  public void simple() throws IOException, LockConflictException {
    final Ref ref = Ref.create("refs/heads/master");

    final HttpReplay replay = YamlHelper.createReplay("/ru/bozaro/gitlfs/client/locking-01.yml");
    final Client client = new Client(new FakeAuthProvider(false), replay);

    final Lock lock = client.lock("build.gradle", ref);
    Assert.assertNotNull(lock);
    Assert.assertNotNull(lock.getId());
    Assert.assertEquals(lock.getPath(), "build.gradle");

    try {
      client.lock("build.gradle", ref);
      Assert.fail();
    } catch (LockConflictException e) {
      Assert.assertEquals(e.getLock().getId(), lock.getId());
    }

    {
      final List<Lock> locks = client.listLocks("build.gradle", null, ref);
      Assert.assertEquals(locks.size(), 1);
      Assert.assertEquals(locks.get(0).getId(), lock.getId());
    }

    {
      final VerifyLocksResult locks = client.verifyLocks(ref);
      Assert.assertEquals(locks.getOurLocks().size(), 2);
      Assert.assertEquals(locks.getOurLocks().get(1).getId(), lock.getId());
      Assert.assertEquals(locks.getTheirLocks().size(), 0);
    }

    final Lock unlock = client.unlock(lock.getId(), true, ref);
    Assert.assertNotNull(unlock);
    Assert.assertEquals(unlock.getId(), lock.getId());

    Assert.assertNull(client.unlock(lock.getId(), false, ref));

    replay.close();
  }
}
