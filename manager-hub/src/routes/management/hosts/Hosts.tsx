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

import React from 'react';
import MainLayout from '../../../views/mainLayout/MainLayout';
import AddButton from "../../../components/form/AddButton";
import styles from './Hosts.module.css'
import CloudHostsList from "./cloud/CloudHostsList";
import EdgeHostsList from "./edge/EdgeHostsList";
import Collapsible from "../../../components/collapsible/Collapsible";
import {ICoordinates} from "../../../components/map/LocationMap";

export interface IHostAddress {
    username: string | undefined;
    publicDnsName: string | undefined;
    publicIpAddress: string;
    privateIpAddress: string | undefined;
    coordinates: ICoordinates;
}

const Hosts = () =>
    <MainLayout>
        <AddButton button={{text: 'New host'}}
                   dropdown={{
                       id: 'hosts',
                       title: 'Select location',
                       data: [
                           {text: 'Cloud', pathname: '/hosts/cloud/new_instance?new'},
                           {text: 'Edge', pathname: '/hosts/edge/add_host?new'}
                       ],
                   }}/>
        <div className={`${styles.container}`}>
            <Collapsible id={"cloudHostsCollapsible"}
                         title={'Cloud'}
                         active
                         headerClassname={styles.collapsibleSubtitle}
                         bodyClassname={styles.collapsibleCardList}>
                <CloudHostsList/>
            </Collapsible>
        </div>
        <div className={`${styles.container}`}>
            <Collapsible id={"edgeHostsCollapsible"}
                         title={'Edge'}
                         active
                         headerClassname={styles.collapsibleSubtitle}
                         bodyClassname={styles.collapsibleCardList}>
                <EdgeHostsList/>
            </Collapsible>
        </div>
    </MainLayout>;

export default Hosts;