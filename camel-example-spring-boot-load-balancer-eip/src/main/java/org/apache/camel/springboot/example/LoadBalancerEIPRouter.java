/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.springboot.example;

import org.apache.camel.builder.RouteBuilder;

import org.springframework.stereotype.Component;

@Component
public class LoadBalancerEIPRouter extends RouteBuilder {
	@Override
	public void configure() throws Exception {
		// @formatter:off

		// round-robin load balancer
		from("direct:loadbalancer-round-robin")
				.loadBalance().roundRobin()
				.to("mock:a")
				.to("mock:b")
				.end();


		// random load balancer
		from("direct:loadbalancer-random")
				.loadBalance().random()
				.to("mock:c")
				.to("mock:d");


		// sticky load balancer
		from("direct:loadbalancer-sticky")
				// load balancer with sticky strategy
				.loadBalance()
				// expression parameter to calculate the correlation key
					.sticky(header("correlation-key"))
				// load balance across 2 producers
					.to("mock:e").to("mock:f")
				.end();


		// topic ("fan out") load-balancer
		from("direct:loadbalancer-topic")
				.loadBalance().topic()
				.to("mock:j", "mock:k");


		// failover load-balancer
		from("direct:loadbalancer-failover")
				.loadBalance()
				// failover on this Exception to subsequent producer
				.failover(MyException.class)
				.to("direct:l", "direct:m");

		from("direct:l")
				.choice()
					.when(body().isEqualTo("E"))
						.throwException(new MyException("direct:l"))
					.end()
				.end()
			.to("mock:l");

		from("direct:m")
				.to("mock:m");
		// END of failover load-balancer



		// failover load-balancer round robin without error handler
		from("direct:loadbalancer-failover-round-robin-no-error-handler")
				.loadBalance()
				// failover immediately in case of exception and do not use errorHandler
				.failover(-1, false, true, MyException.class, Exception.class)
				.to("direct:n", "direct:o", "direct:p", "direct:q");

		from("direct:n")
				.choice()
					.when(constant(true))
						.throwException(new MyException("from direct:n"))
					.end()
				.end()
			.to("mock:n");

		from("direct:o")
				.choice()
					.when(constant(true))
						.throwException(new MyException("from direct:n"))
					.end()
				.end()
			.to("mock:o");

		from("direct:p")
				.choice()
					.when(body().isEqualTo("E"))
						.throwException(new RuntimeException())
					.end()
				.end()
			.to("mock:p");

		from("direct:q")
				.to("mock:q");
		// END of failover load-balancer round robin without error handler


		// weighted load-balancer round robin
		String distributionRatio = "2,1";

		from("direct:loadbalancer-weighted-round-robin")
				.loadBalance().weighted(true, distributionRatio)
				.to("mock:w", "mock:x");
		// ENF of weighted load-balancer round robin


		// custom load balancer
		from ("direct:loadbalancer-custom")
				// custom load balancer
				.loadBalance(new CustomLoadBalancer())
				.to("mock:g", "mock:h");

		// @formatter:off
	}

}
