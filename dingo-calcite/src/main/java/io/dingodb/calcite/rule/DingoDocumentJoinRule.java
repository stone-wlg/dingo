/*
 * Copyright 2021 DataCanvas
 *
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

package io.dingodb.calcite.rule;

import io.dingodb.calcite.rel.LogicalDingoDocument;
import io.dingodb.calcite.rule.dingo.DingoHashJoinRule;
import io.dingodb.calcite.traits.DingoConvention;
import io.dingodb.calcite.traits.DingoRelStreaming;
import io.dingodb.calcite.type.DingoSqlTypeFactory;
import org.apache.calcite.plan.RelOptRuleCall;
import org.apache.calcite.plan.RelRule;
import org.apache.calcite.plan.RelTraitSet;
import org.apache.calcite.plan.volcano.RelSubset;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.logical.LogicalJoin;
import org.apache.calcite.rex.RexBuilder;
import org.immutables.value.Value;


@Value.Enclosing
public class DingoDocumentJoinRule extends RelRule<DingoDocumentJoinRule.Config>  {
    /**
     * Creates a RelRule.
     *
     * @param config config
     */
    public DingoDocumentJoinRule(Config config) {
        super(config);
    }

    @Override
    public void onMatch(RelOptRuleCall call) {
        LogicalJoin logicalJoin = call.rel(0);

        LogicalJoin newLogicalJoin = (LogicalJoin) logicalJoin.copy(logicalJoin.getTraitSet(), logicalJoin.getInputs());

        RelSubset subset = (RelSubset) newLogicalJoin.getLeft();
        if (subset.getRelList().size() == 1) {
            RelNode relNode = subset.getBestOrOriginal();
            if (relNode instanceof LogicalDingoDocument) {
                LogicalDingoDocument document = (LogicalDingoDocument) relNode;
                RelTraitSet traits = document.getTraitSet()
                    .replace(DingoConvention.INSTANCE)
                    .replace(DingoRelStreaming.of(document.getTable()));
                LogicalDingoDocument replaceDoc = (LogicalDingoDocument) document.copy(traits, document.getInputs(),
                    document.getCall(), document.getElementType(), document.getRowType(), document.getColumnMappings());
                RexBuilder rexBuilder = new RexBuilder(DingoSqlTypeFactory.INSTANCE);
                call.transformTo(newLogicalJoin);
            }
        }
    }

    @Value.Immutable
    public interface Config extends RelRule.Config {
        DingoDocumentJoinRule.Config DEFAULT = ImmutableDingoDocumentJoinRule.Config.builder()
            .operandSupplier(b0 ->
                b0.operand(LogicalJoin.class).predicate(rel -> {
                    if (!DingoHashJoinRule.match(rel)) {
                        return false;
                    }
                    if (rel.getHints().size() == 0) {
                        return false;
                    } else {
                        if (!"text_search_pre".equalsIgnoreCase(rel.getHints().get(0).hintName)) {
                            return false;
                        }
                    }
                    if (rel.getLeft() instanceof RelSubset) {
                        RelSubset subset = (RelSubset) rel.getLeft();
                        if (subset.getRelList().size() == 1) {
                            RelNode relNode = subset.getBestOrOriginal();
                            if (relNode instanceof LogicalDingoDocument) {
                                return true;
                            }
                        }
                    }
                    return false;
                }).anyInputs()
            )
            .description("DingoDocumentJoinRule")
            .build();

        @Override
        default DingoDocumentJoinRule toRule() {
            return new DingoDocumentJoinRule(this);
        }
    }
}
