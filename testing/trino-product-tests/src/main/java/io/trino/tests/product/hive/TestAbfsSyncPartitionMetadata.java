/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.trino.tests.product.hive;

import io.trino.tempto.AfterTestWithContext;
import io.trino.tempto.BeforeTestWithContext;
import io.trino.testng.services.Flaky;
import org.testng.annotations.Test;

import static com.google.common.base.Preconditions.checkArgument;
import static io.trino.testing.TestingNames.randomNameSuffix;
import static io.trino.tests.product.TestGroups.AZURE;
import static io.trino.tests.product.utils.HadoopTestUtils.ERROR_COMMITTING_WRITE_TO_HIVE_ISSUE;
import static io.trino.tests.product.utils.HadoopTestUtils.ERROR_COMMITTING_WRITE_TO_HIVE_MATCH;
import static io.trino.tests.product.utils.QueryExecutors.onHive;
import static io.trino.tests.product.utils.QueryExecutors.onTrino;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static org.apache.parquet.Strings.isNullOrEmpty;

public class TestAbfsSyncPartitionMetadata
        extends BaseTestSyncPartitionMetadata
{
    private final String schema = "test_" + randomNameSuffix();

    @BeforeTestWithContext
    public void setUp()
    {
        removeHdfsDirectory(schemaLocation());
        makeHdfsDirectory(schemaLocation());
    }

    @AfterTestWithContext
    public void tearDown()
    {
        removeHdfsDirectory(schemaLocation());
    }

    @Override
    protected String schemaLocation()
    {
        String container = requireNonNull(System.getenv("ABFS_CONTAINER"), "Environment variable not set: ABFS_CONTAINER");
        String account = requireNonNull(System.getenv("ABFS_ACCOUNT"), "Environment variable not set: ABFS_ACCOUNT");
        return format("abfs://%s@%s.dfs.core.windows.net/%s", container, account, schema);
    }

    @Test(groups = AZURE)
    @Flaky(issue = ERROR_COMMITTING_WRITE_TO_HIVE_ISSUE, match = ERROR_COMMITTING_WRITE_TO_HIVE_MATCH)
    @Override
    public void testAddPartition()
    {
        super.testAddPartition();
    }

    @Test(groups = AZURE)
    @Flaky(issue = ERROR_COMMITTING_WRITE_TO_HIVE_ISSUE, match = ERROR_COMMITTING_WRITE_TO_HIVE_MATCH)
    @Override
    public void testAddPartitionContainingCharactersThatNeedUrlEncoding()
    {
        super.testAddPartitionContainingCharactersThatNeedUrlEncoding();
    }

    @Test(groups = AZURE)
    @Flaky(issue = ERROR_COMMITTING_WRITE_TO_HIVE_ISSUE, match = ERROR_COMMITTING_WRITE_TO_HIVE_MATCH)
    @Override
    public void testDropPartition()
    {
        super.testDropPartition();
    }

    @Test(groups = AZURE)
    @Flaky(issue = ERROR_COMMITTING_WRITE_TO_HIVE_ISSUE, match = ERROR_COMMITTING_WRITE_TO_HIVE_MATCH)
    @Override
    public void testDropPartitionContainingCharactersThatNeedUrlEncoding()
    {
        super.testDropPartitionContainingCharactersThatNeedUrlEncoding();
    }

    @Test(groups = AZURE)
    @Flaky(issue = ERROR_COMMITTING_WRITE_TO_HIVE_ISSUE, match = ERROR_COMMITTING_WRITE_TO_HIVE_MATCH)
    @Override
    public void testFullSyncPartition()
    {
        super.testFullSyncPartition();
    }

    @Test(groups = AZURE)
    @Flaky(issue = ERROR_COMMITTING_WRITE_TO_HIVE_ISSUE, match = ERROR_COMMITTING_WRITE_TO_HIVE_MATCH)
    @Override
    public void testInvalidSyncMode()
    {
        super.testInvalidSyncMode();
    }

    @Test(groups = AZURE)
    @Flaky(issue = ERROR_COMMITTING_WRITE_TO_HIVE_ISSUE, match = ERROR_COMMITTING_WRITE_TO_HIVE_MATCH)
    @Override
    public void testMixedCasePartitionNames()
    {
        super.testMixedCasePartitionNames();
    }

    @Test(groups = AZURE)
    @Flaky(issue = ERROR_COMMITTING_WRITE_TO_HIVE_ISSUE, match = ERROR_COMMITTING_WRITE_TO_HIVE_MATCH)
    @Override
    public void testConflictingMixedCasePartitionNames()
    {
        super.testConflictingMixedCasePartitionNames();
    }

    @Test(groups = AZURE)
    @Override
    public void testSyncPartitionMetadataWithNullArgument()
    {
        super.testSyncPartitionMetadataWithNullArgument();
    }

    @Override
    protected void removeHdfsDirectory(String path)
    {
        checkArgument(!isNullOrEmpty(path) && !path.equals("/"));
        onHive().executeQuery("dfs -rm -f -r " + path);
    }

    @Override
    protected void makeHdfsDirectory(String path)
    {
        onHive().executeQuery("dfs -mkdir -p " + path);
    }

    @Override
    protected void copyOrcFileToHdfsDirectory(String tableName, String targetDirectory)
    {
        String orcFilePath = generateOrcFile();
        onHive().executeQuery(format("dfs -cp %s %s", orcFilePath, targetDirectory));
    }

    @Override
    protected void createTable(String tableName, String tableLocation)
    {
        makeHdfsDirectory(tableLocation);
        onHive().executeQuery("CREATE TABLE " + tableName + " (payload bigint) PARTITIONED BY (col_x string, col_y string) STORED AS ORC LOCATION '" + tableLocation + "'");
    }

    // Drop and create a table. Then, return single ORC file path
    private String generateOrcFile()
    {
        onTrino().executeQuery("DROP TABLE IF EXISTS single_int_column");
        onTrino().executeQuery("CREATE TABLE single_int_column (payload bigint) WITH (format = 'ORC')");
        onTrino().executeQuery("INSERT INTO single_int_column VALUES (42)");
        return (String) onTrino().executeQuery("SELECT \"$path\" FROM single_int_column").getOnlyValue();
    }
}
