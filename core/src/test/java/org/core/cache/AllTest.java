package org.core.cache;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;


@RunWith(Suite.class)
@SuiteClasses({ CacheElementTest.class, DummyCacheTest.class,
		RandomCacheTest.class })
public class AllTest {

}