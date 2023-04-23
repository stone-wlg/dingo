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

package io.dingodb.exec.base;

import io.dingodb.common.CommonId;
import io.dingodb.common.Location;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@EqualsAndHashCode
public class OutputHint {
    /**
     * Used by PartModify.
     */
    @Getter
    @Setter
    private CommonId partId = null;
    /**
     * Used by Exchange.
     */
    @Getter
    @Setter
    private Location location = null;
    /**
     * Used by Coalesce.
     */
    @Getter
    @Setter
    private boolean toSumUp = false;
}
