FROM node:boron

# Create app directory
RUN mkdir -p /usr/src/webacapp
WORKDIR /usr/src/webacapp

# Install app dependencies
COPY package.json /usr/src/webacapp/
RUN npm install
# Bundle app source
COPY . /usr/src/webacapp
EXPOSE 9003
CMD [ "node", "index.js"]