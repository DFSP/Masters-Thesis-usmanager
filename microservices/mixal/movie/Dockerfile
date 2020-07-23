FROM node:boron

# Create app directory
RUN mkdir -p /usr/src/movies_app
WORKDIR /usr/src/movies_app

# Install app dependencies
COPY package.json /usr/src/movies_app/
RUN npm install
# Bundle app source
COPY . /usr/src/movies_app
EXPOSE 9002

CMD [ "npm", "start"]