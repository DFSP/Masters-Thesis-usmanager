import Card from "../../../components/cards/Card";
import React from "react";
import {IApp} from "./App";

interface AppCardProps {
  app: IApp;
}

type Props = AppCardProps;

const CardApp = Card<IApp>();
const AppCard = ({app}: Props) => (
  <CardApp title={app.name}
           link={{ to: { pathname: `/apps/${app.name}`, state: app } }}
           height={'30px'}
           margin={'10px 0'}
           hoverable/>
);

export default AppCard;