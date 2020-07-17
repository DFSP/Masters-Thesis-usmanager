import Card from "../../../components/cards/Card";
import CardItem from "../../../components/list/CardItem";
import React from "react";
import {INode} from "./Node";
import {ISimulatedServiceMetric} from "../metrics/services/SimulatedServiceMetric";

interface NodeCardProps {
  node: INode;
}

type Props = NodeCardProps;

const CardNode = Card<INode>();
const NodeCard = ({node}: Props) => (
  <CardNode title={node.id.toString()}
            link={{to: {pathname: `/nodes/${node.id}`, state: node}}}
            height={'150px'}
            margin={'10px 0'}
            hoverable>
    <CardItem key={'hostName'}
              label={'Hostname'}
              value={node.hostname}/>
    <CardItem key={'state'}
              label={'State'}
              value={node.state}/>
    <CardItem key={'availability'}
              label={'Availability'}
              value={node.availability}/>
    <CardItem key={'role'}
              label={'Role'}
              value={node.role}/>
  </CardNode>
);

export default NodeCard;
