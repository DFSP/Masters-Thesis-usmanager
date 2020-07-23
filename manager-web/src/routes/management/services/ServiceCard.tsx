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
import CardItem from '../../../components/list/CardItem';
import {IService} from "./Service";
import Card from "../../../components/cards/Card";

interface ServiceCardProps {
  service: IService;
}

type Props = ServiceCardProps;

const getReplicasMessage = (minReplicas: number, maxReplicas: number): string => {
  if (minReplicas === maxReplicas) {
    return `${minReplicas}`;
  } else if (maxReplicas === 0) {
    return `At least ${minReplicas}`
  } else {
    return `At least ${minReplicas} up to ${maxReplicas}`;
  }
};

const CardService = Card<IService>();
const ServiceCard = ({service}: Props) => (
  <CardService title={service.serviceName}
               link={{to: {pathname: `/services/${service.serviceName}`, state: service}}}
               height={'250px'}
               margin={'10px 0'}
               hoverable>
    <CardItem key={'serviceType'}
              label={'Service type'}
              value={`${service.serviceType}`}/>
    <CardItem key={'replicas'}
              label={'Replicas'}
              value={getReplicasMessage(service.minReplicas, service.maxReplicas)}/>
    <CardItem key={'ports'}
              label={'Ports'}
              value={`${service.defaultExternalPort}:${service.defaultInternalPort}`}/>
    {service.launchCommand !== '' &&
     <CardItem key={'launchCommand'}
               label={'Launch command'}
               value={service.launchCommand}/>}
    <CardItem key={'outputLabel'}
              label={'Output label'}
              value={`${service.outputLabel}`}/>
    {service.defaultDb !== 'NOT_APPLICABLE' &&
     <CardItem key={'database'}
               label={'Database'}
               value={service.defaultDb}/>}
    <CardItem key={'memory'}
              label={'Memory'}
              value={`${service.expectedMemoryConsumption} bytes`}/>
  </CardService>
);

export default ServiceCard;
