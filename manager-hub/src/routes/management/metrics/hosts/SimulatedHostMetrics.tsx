/*
 * MIT License
 *
 * Copyright (c) 2020 manager
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

import React from "react";
import MainLayout from "../../../../views/mainLayout/MainLayout";
import AddButton from "../../../../components/form/AddButton";
import styles from './SimulatedHostMetrics.module.css'
import SimulatedHostMetricsList from "./SimulatedHostMetricsList";

const SimulatedHostMetrics: React.FC = () =>
    <MainLayout>
        <AddButton tooltip={{text: 'Add simulated host metric', position: 'left'}}
                   pathname={'/simulated-metrics/hosts/add_simulated_metric?new'}/>
        <div className={`${styles.container}`}>
            <SimulatedHostMetricsList/>
        </div>
    </MainLayout>;

export default SimulatedHostMetrics;