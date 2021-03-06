/*
 * Copyright (c) 2017 "Neo4j, Inc." <http://neo4j.com>
 *
 * This file is part of Neo4j Graph Algorithms <http://github.com/neo4j-contrib/neo4j-graph-algorithms>.
 *
 * Neo4j Graph Algorithms is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.neo4j.graphalgo.algo;

import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.graphalgo.InfoMapProc;
import org.neo4j.graphalgo.api.Graph;
import org.neo4j.graphalgo.helper.ldbc.LdbcDownloader;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.internal.kernel.api.exceptions.KernelException;
import org.neo4j.kernel.impl.proc.Procedures;
import org.neo4j.kernel.internal.GraphDatabaseAPI;

import java.io.IOException;

/**
 * @author mknblch
 */
public class InfoMapYelpTest {

    private static GraphDatabaseService db;

    private static Graph graph;

    @BeforeClass
    public static void setupGraph() throws KernelException, IOException {
        db = LdbcDownloader.openDb("Yelp");
        Procedures proceduresService = ((GraphDatabaseAPI) db).getDependencyResolver().resolveDependency(Procedures.class);
        proceduresService.registerProcedure(InfoMapProc.class, true);
    }

    @Test
    public void testWeighted() throws Exception {
        db.execute("CALL algo.infoMap('MATCH (c:Category) RETURN id(c) AS id',\n" +
                "  'MATCH (c1:Category)<-[:IN_CATEGORY]-()-[:IN_CATEGORY]->(c2:Category)\n" +
                "   WHERE id(c1) < id(c2)\n" +
                "   RETURN id(c1) AS source, id(c2) AS target, count(*) AS w', " +
                " {graph: 'cypher', iterations:15, writeProperty:'c', threshold:0.01, tau:0.3, weightProperty:'w', concurrency:4})").accept(row -> {
            System.out.println("computeMillis = " + row.get("computeMillis"));
            System.out.println("nodeCount = " + row.get("nodeCount"));
            System.out.println("iterations = " + row.get("iterations"));
            System.out.println("communityCount = " + row.get("communityCount"));
            return true;
        });
    }

    @Test
    public void testUnweighted() throws Exception {
        db.execute("CALL algo.infoMap('MATCH (c:Category) RETURN id(c) AS id',\n" +
                "  'MATCH (c1:Category)<-[:IN_CATEGORY]-()-[:IN_CATEGORY]->(c2:Category)\n" +
                "   WHERE id(c1) < id(c2)\n" +
                "   RETURN id(c1) AS source, id(c2) AS target', " +
                " {graph: 'cypher', iterations:15, writeProperty:'c', threshold:0.01, tau:0.3, concurrency:4})").accept(row -> {
            System.out.println("computeMillis = " + row.get("computeMillis"));
            System.out.println("nodeCount = " + row.get("nodeCount"));
            System.out.println("iterations = " + row.get("iterations"));
            System.out.println("communityCount = " + row.get("communityCount"));
            return true;
        });
    }
}
