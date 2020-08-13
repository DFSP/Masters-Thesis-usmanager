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

import Card from "../../../../components/cards/Card";
import CardItem from "../../../../components/list/CardItem";
import React from "react";
import {ICloudHost} from "./CloudHost";

interface CloudHostCardProps {
  cloudHost: ICloudHost;
}

type Props = CloudHostCardProps;

const CardCloudHost = Card<ICloudHost>();
const CloudHostCard = ({cloudHost}: Props) => (
  <CardCloudHost title={cloudHost.instanceId}
                 link={{to: {pathname: `/hosts/cloud/${cloudHost.instanceId}`, state: cloudHost}}}
                 height={'185px'}
                 margin={'10px 0'}
                 hoverable>
    <CardItem key={'imageId'}
              label={'imageId'}
              value={`${cloudHost.imageId}`}/>
    <CardItem key={'instanceType'}
              label={'instanceType'}
              value={`${cloudHost.instanceType}`}/>
    <CardItem key={'state'}
              label={'state'}
              value={`${cloudHost.state.name}`}/>
    {cloudHost.publicDnsName &&
     <CardItem key={'publicDnsName'}
               label={'publicDnsName'}
               value={`${cloudHost.publicDnsName}`}/>}
    {cloudHost.publicIpAddress &&
     <CardItem key={'publicIpAddress'}
               label={'publicIpAddress'}
               value={`${cloudHost.publicIpAddress}`}/>}
    {cloudHost.privateIpAddress &&
     <CardItem key={'privateIpAddress'}
               label={'privateIpAddress'}
               value={`${cloudHost.privateIpAddress}`}/>}
    <CardItem key={'placement'}
              label={'placement'}
              value={`${cloudHost.placement.availabilityZone}`}/>
    {cloudHost.managedByWorker &&
     <CardItem key={'managedByWorker'}
               label={'managedByWorker'}
               value={`${cloudHost.managedByWorker.id}`}/>}
  </CardCloudHost>
);

export default CloudHostCard;