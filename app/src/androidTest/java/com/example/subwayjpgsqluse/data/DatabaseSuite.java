package com.example.subwayjpgsqluse.data;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({DatabaseWriteTest.class, DatabaseQueryTest.class})
public final class DatabaseSuite {}
