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
import CardItem from "../../../components/list/CardItem";
import Card from "../../../components/cards/Card";
import {IEurekaServer} from "./EurekaServer";
import {IContainer} from "../containers/Container";

interface EurekaServerCardProps {
  eurekaServer: IEurekaServer;
}

type Props = EurekaServerCardProps;

const CardEurekaServer = Card<IContainer>();
const EurekaServerCard = ({eurekaServer}: Props) => (
  <CardEurekaServer title={eurekaServer.containerId.toString()}
                    link={{to: {pathname: `/eureka-servers/${eurekaServer.containerId}`, state: eurekaServer}}}
                    height={'125px'}
                    margin={'10px 0'}
                    hoverable>
    <CardItem key={'hostname'}
              label={'Hostname'}
              value={eurekaServer.hostname}/>
    <CardItem key={'ports'}
              label={'Ports'}
              value={`${eurekaServer.ports.map(p => `${p.privatePort}:${p.publicPort}`).join('/')}`}/>
  </CardEurekaServer>
);

export default EurekaServerCard;