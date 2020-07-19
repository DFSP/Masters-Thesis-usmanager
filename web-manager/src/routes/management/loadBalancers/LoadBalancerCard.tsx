import React from "react";
import CardItem from "../../../components/list/CardItem";
import Card from "../../../components/cards/Card";
import {ILoadBalancer} from "./LoadBalancer";
import {IContainer} from "../containers/Container";

interface LoadBalancerCardProps {
  loadBalancer: ILoadBalancer;
}

type Props = LoadBalancerCardProps;

const CardLoadBalancer = Card<IContainer>();
const LoadBalancerCard = ({loadBalancer}: Props) => (
  <CardLoadBalancer title={loadBalancer.containerId.toString()}
                    link={{to: {pathname: `/load-balancers/${loadBalancer.containerId}`, state: loadBalancer}}}
                    height={'125px'}
                    margin={'10px 0'}
                    hoverable>
    <CardItem key={'hostname'}
              label={'Hostname'}
              value={loadBalancer.hostname}/>
    <CardItem key={'ports'}
              label={'Ports'}
              value={`${loadBalancer.ports.map(p => `${p.privatePort}:${p.publicPort}`).join('/')}`}/>
  </CardLoadBalancer>
);

export default LoadBalancerCard;