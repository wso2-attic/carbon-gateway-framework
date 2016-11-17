/*
*  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.ballerina.core.flow;


import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.wso2.ballerina.core.flow.templates.uri.URITemplate;
import org.wso2.ballerina.core.flow.templates.uri.parser.FragmentExpression;
import org.wso2.ballerina.core.flow.templates.uri.parser.LabelExpression;
import org.wso2.ballerina.core.flow.templates.uri.parser.PathSegmentExpression;
import org.wso2.ballerina.core.flow.templates.uri.parser.ReservedStringExpression;
import org.wso2.ballerina.core.flow.templates.uri.parser.SimpleStringExpression;

import java.util.HashMap;
import java.util.Map;

public class UriParserTest {

    Map<String, String> variables = new HashMap<String, String>();

    @Before
    public void setUp() throws Exception {
        variables.put("dom", "example.com");
        variables.put("dub", "me/too");
        variables.put("hello", "Hello World!");
        variables.put("half", "50%");
        variables.put("var", "value");
        variables.put("who", "fred");
        variables.put("base", "http://example.com/home/");
        variables.put("path", "/foo/bar");
        variables.put("v", "6");
        variables.put("x", "1024");
        variables.put("y", "768");
        variables.put("empty", "");
    }

    @Test
    public void testSimpleStringExpansion() throws Exception {
        Assert.assertEquals("value", new SimpleStringExpression("var").expand(variables));
        Assert.assertEquals("Hello%20World%21", new SimpleStringExpression("hello").expand(variables));
        Assert.assertEquals("50%25", new SimpleStringExpression("half").expand(variables));
        Assert.assertEquals("", new SimpleStringExpression("empty").expand(variables));
        Assert.assertNull(new SimpleStringExpression("undef").expand(variables));
        Assert.assertEquals("1024,768", new SimpleStringExpression("x,y").expand(variables));
        Assert.assertEquals("1024,Hello%20World%21,768", new SimpleStringExpression("x,hello,y").expand(variables));
        Assert.assertEquals("1024,", new SimpleStringExpression("x,empty").expand(variables));
        Assert.assertEquals("1024", new SimpleStringExpression("x,undef").expand(variables));
        Assert.assertEquals("768", new SimpleStringExpression("undef,y").expand(variables));
        Assert.assertEquals("768", new SimpleStringExpression("undef,y").expand(variables));
        Assert.assertEquals("val", new SimpleStringExpression("var:3").expand(variables));
        Assert.assertEquals("value", new SimpleStringExpression("var:30").expand(variables));
        Assert.assertEquals("http%3A%2F%2Fexample.com%2Fhome%2F", new SimpleStringExpression("base").expand(variables));
    }

    @Test
    public void testSimpleStringMatch() throws Exception {
        URITemplate template = new URITemplate("/admin/~{user}");
        Map<String, String> var = new HashMap<String, String>();
        Assert.assertTrue(template.matches("/admin/~hiranya", var));
        Assert.assertEquals("hiranya", var.get("user"));
        Assert.assertFalse(template.matches("/admi/~hiranya", var));
        Assert.assertFalse(template.matches("/admin/hiranya", var));
        Assert.assertFalse(template.matches("/admin/~hiranya/foo", var));
        Assert.assertFalse(template.matches("/admin/~hirany.a", var));
        var.clear();

        template = new URITemplate("/dictionary/{char:1}/{word}");
        Assert.assertTrue(template.matches("/dictionary/c/cat", var));
        Assert.assertEquals("c", var.get("char"));
        Assert.assertEquals("cat", var.get("word"));
        Assert.assertFalse(template.matches("/dictionry/c/cat", var));
        Assert.assertFalse(template.matches("/dictionary/c", var));
        Assert.assertFalse(template.matches("/dictionary/co/cat", var));
        var.clear();

        Assert.assertTrue(template.matches("/dictionary/h/hello%20world", var));
        Assert.assertEquals("h", var.get("char"));
        Assert.assertEquals("hello world", var.get("word"));
        var.clear();

        template = new URITemplate("/dictionary/{char}/{+word}");
        Assert.assertTrue(template.matches("/dictionary/h/hello+world", var));
        Assert.assertEquals("h", var.get("char"));
        Assert.assertEquals("hello+world", var.get("word"));
        var.clear();

        Assert.assertTrue(template.matches("/dictionary/h/hello world", var));
        Assert.assertEquals("h", var.get("char"));
        Assert.assertEquals("hello world", var.get("word"));
        var.clear();

        Assert.assertTrue(template.matches("/dictionary/h/hello%2Bworld", var));
        Assert.assertEquals("h", var.get("char"));
        Assert.assertEquals("hello+world", var.get("word"));
        var.clear();

        template = new URITemplate("/dictionary/{char}/{word,count}");
        Assert.assertTrue(template.matches("/dictionary/c/cat,5", var));
        Assert.assertEquals("c", var.get("char"));
        Assert.assertEquals("cat", var.get("word"));
        Assert.assertEquals("5", var.get("count"));
        var.clear();

        Assert.assertTrue(template.matches("/dictionary/c/cat", var));
        Assert.assertEquals("c", var.get("char"));
        Assert.assertEquals("cat", var.get("word"));
        Assert.assertEquals("", var.get("count"));
        var.clear();

        Assert.assertTrue(template.matches("/dictionary/c/,5", var));
        Assert.assertEquals("c", var.get("char"));
        Assert.assertEquals("", var.get("word"));
        Assert.assertEquals("5", var.get("count"));
        var.clear();

        template = new URITemplate("/dictionary/{char,word}/{count}");
        Assert.assertTrue(template.matches("/dictionary/c,cat/5", var));
        Assert.assertEquals("c", var.get("char"));
        Assert.assertEquals("cat", var.get("word"));
        Assert.assertEquals("5", var.get("count"));
        var.clear();

        Assert.assertTrue(template.matches("/dictionary/c/5", var));
        Assert.assertEquals("c", var.get("char"));
        Assert.assertEquals("", var.get("word"));
        Assert.assertEquals("5", var.get("count"));
        Assert.assertFalse(template.matches("/dictionary/c,ca,cat/5", var));
        var.clear();

        template = new URITemplate("/dictionary/{user}/test?a={user}");
        Assert.assertTrue(template.matches("/dictionary/hiranya/test?a=hiranya", var));
        Assert.assertEquals("hiranya", var.get("user"));
        Assert.assertFalse(template.matches("/dictionary/hiranya/test?a=foo", var));
        var.clear();

        template = new URITemplate("/dictionary/foo-{user}-bar");
        Assert.assertTrue(template.matches("/dictionary/foo-hiranya-bar", var));
        Assert.assertEquals("hiranya", var.get("user"));
        var.clear();
        Assert.assertTrue(template.matches("/dictionary/foo--bar", var));
        Assert.assertFalse(template.matches("/dictionary/foo-bar", var));

        template = new URITemplate("/alert/{id}.json");
        Assert.assertTrue(template.matches("/alert/foo.json", var));
        Assert.assertEquals("foo", var.get("id"));
        var.clear();

        template = new URITemplate("/");
        Assert.assertTrue(template.matches("/", var));

        template = new URITemplate("/*");
        Assert.assertTrue(template.matches("/sanjeewa?test=done", var));
        Assert.assertTrue(template.matches("/sanjeewa", var));
        Assert.assertTrue(template.matches("/", var));

        template = new URITemplate("/sanjeewa/*");
        Assert.assertTrue(template.matches("/sanjeewa/admin?test=done", var));
        Assert.assertTrue(template.matches("/sanjeewa/test", var));

        template = new URITemplate("/sanjeewa*");
        Assert.assertTrue(template.matches("/sanjeewa/admin?test=done", var));
        Assert.assertTrue(template.matches("/sanjeewa/test", var));
        Assert.assertTrue(template.matches("/sanjeewa/", var));
        Assert.assertTrue(template.matches("/sanjeewa", var));

        template = new URITemplate("/{sanjeewa}/*");
        Assert.assertTrue(template.matches("/sanjeewa/admin?test=done", var));
        Assert.assertTrue(template.matches("/sanjeewa/?test=done", var));
        Assert.assertTrue(template.matches("/sanjeewa/test", var));
        Assert.assertTrue(template.matches("/sanjeewa/", var));

        template = new URITemplate("/dictionary/{char}/{word}");
        Assert.assertTrue(template.matches("/dictionary/d/dog/", var));
        Assert.assertTrue(template.matches("/dictionary/d/dog", var));

        template = new URITemplate("/test{format}*");
        Assert.assertTrue(template.matches("/test.json?test", var));
        Assert.assertTrue(template.matches("/test.json/", var));
        Assert.assertTrue(template.matches("/test.json", var));


        template = new URITemplate("/test{format}/*");
        Assert.assertTrue(template.matches("/test.json/test", var));
        Assert.assertTrue(template.matches("/test.json/", var));
        Assert.assertTrue(template.matches("/test.json", var));


        template = new URITemplate("/sanjeewa/~{test}?*");
        var.put("test", "tester");
        Assert.assertTrue(template.matches("/sanjeewa/~tester?test", var));
        var.clear();

        template = new URITemplate("/sanjeewa/{name,id}/*");
        var.put("name", "user");
        var.put("id", "190");
        // matching resource urls
        Assert.assertTrue(template.matches("/sanjeewa/user,190/test", var));
        Assert.assertTrue(template.matches("/sanjeewa/user,190/test?year=2012", var));
        var.clear();

        template = new URITemplate("/{name,id}/*");
        var.put("name", "user");
        var.put("id", "190");
        // matching resource urls
        Assert.assertTrue(template.matches("/user,190/test", var));
        Assert.assertTrue(template.matches("/user,190/test?year=2012", var));
        // un matching resource urls
        Assert.assertFalse(template.matches("/sanjeewa/user,190", var));
        Assert.assertFalse(template.matches("/sanjeewa/user,190,11/test", var));
        Assert.assertFalse(template.matches("/sanjeewa/user/test", var));
        Assert.assertFalse(template.matches("/rangana/user,190/", var));
        Assert.assertFalse(template.matches("/sanjeewa/test", var));
        var.clear();

    }

    @Test
    public void testReservedStringExpansion() throws Exception {
        Assert.assertEquals("value", new ReservedStringExpression("var").expand(variables));
        Assert.assertEquals("Hello%20World!", new ReservedStringExpression("hello").expand(variables));
        Assert.assertEquals("50%25", new ReservedStringExpression("half").expand(variables));
        Assert.assertEquals("http://example.com/home/", new ReservedStringExpression("base").expand(variables));
        Assert.assertEquals("", new ReservedStringExpression("empty").expand(variables));
        Assert.assertNull(new ReservedStringExpression("undef").expand(variables));
        Assert.assertEquals("/foo/bar", new ReservedStringExpression("path").expand(variables));
        Assert.assertEquals("1024,Hello%20World!,768", new ReservedStringExpression("x,hello,y").expand(variables));
        Assert.assertEquals("/foo/bar,1024", new ReservedStringExpression("path,x").expand(variables));
        Assert.assertEquals("/foo/b", new ReservedStringExpression("path:6").expand(variables));
    }

    @Test
    public void testReservedStringMatch() throws Exception {
        URITemplate template = new URITemplate("/admin/~{+user}");
        Map<String, String> var = new HashMap<String, String>();
        Assert.assertTrue(template.matches("/admin/~foo!bar", var));
        Assert.assertEquals("foo!bar", var.get("user"));
        Assert.assertFalse(template.matches("/admi/~hiranya", var));
        Assert.assertFalse(template.matches("/admin/hiranya", var));
        Assert.assertFalse(template.matches("/admin/~hiranya/foo", var));
        var.clear();

        template = new URITemplate("/words?{+query}");
        Assert.assertTrue(template.matches("/words?a=5", var));
        Assert.assertEquals("a=5", var.get("query"));
        var.clear();

        template = new URITemplate("/{symbol}/feed.rss{+queryStr}");
        Assert.assertTrue(template.matches("/APPLE/feed.rss?max=30", var));
        Assert.assertEquals("?max=30", var.get("queryStr"));
        var.clear();
        Assert.assertFalse(template.matches("/APPLE?max=30", var));
    }

    @Test
    public void testFragmentExpansion() throws Exception {
        Assert.assertEquals("#value", new FragmentExpression("var").expand(variables));
        Assert.assertEquals("#Hello%20World!", new FragmentExpression("hello").expand(variables));
        Assert.assertEquals("#50%25", new FragmentExpression("half").expand(variables));
        Assert.assertEquals("#", new FragmentExpression("empty").expand(variables));
        Assert.assertNull(new FragmentExpression("undef").expand(variables));
        Assert.assertEquals("#1024,Hello%20World!,768", new FragmentExpression("x,hello,y").expand(variables));
        Assert.assertEquals("#/foo/bar,1024", new FragmentExpression("path,x").expand(variables));
        Assert.assertEquals("#/foo/b", new FragmentExpression("path:6").expand(variables));
    }

    @Test
    public void testFragmentMatch() throws Exception {
        URITemplate template = new URITemplate("/admin{#foo}");
        Map<String, String> var = new HashMap<String, String>();
        Assert.assertTrue(template.matches("/admin#test", var));
        Assert.assertEquals("test", var.get("foo"));
        var.clear();

        Assert.assertFalse(template.matches("/admin/test", var));
        Assert.assertTrue(template.matches("/admin#test,value", var));
        Assert.assertEquals("test,value", var.get("foo"));
    }

    @Test
    public void testLabelExpansion() throws Exception {
        Assert.assertEquals(".fred", new LabelExpression("who").expand(variables));
        Assert.assertEquals(".fred.fred", new LabelExpression("who,who").expand(variables));
        Assert.assertEquals(".50%25.fred", new LabelExpression("half,who").expand(variables));
        Assert.assertEquals(".example.com", new LabelExpression("dom").expand(variables));
        Assert.assertEquals(".", new LabelExpression("empty").expand(variables));
        Assert.assertNull(new LabelExpression("undef").expand(variables));
        Assert.assertEquals(".val", new LabelExpression("var:3").expand(variables));
    }

    @Test
    public void testLabelMatch() throws Exception {
        URITemplate template = new URITemplate("/admin{.action}");
        Map<String, String> var = new HashMap<String, String>();
        Assert.assertTrue(template.matches("/admin.do", var));
        Assert.assertEquals("do", var.get("action"));
        Assert.assertFalse(template.matches("/admin.do.bad", var));
        var.clear();

        template = new URITemplate("/admin{.action,sub}");
        Assert.assertTrue(template.matches("/admin.do.view", var));
        Assert.assertEquals("do", var.get("action"));
        Assert.assertEquals("view", var.get("sub"));
    }

    @Test
    public void testPathSegmentExpansion() throws Exception {
        Assert.assertEquals("/fred", new PathSegmentExpression("who").expand(variables));
        Assert.assertEquals("/fred/fred", new PathSegmentExpression("who,who").expand(variables));
        Assert.assertEquals("/50%25/fred", new PathSegmentExpression("half,who").expand(variables));
        Assert.assertEquals("/fred/me%2Ftoo", new PathSegmentExpression("who,dub").expand(variables));
        Assert.assertEquals("/value/", new PathSegmentExpression("var,empty").expand(variables));
        Assert.assertEquals("/value", new PathSegmentExpression("var,undef").expand(variables));
        Assert.assertEquals("/value/1024", new PathSegmentExpression("var,x").expand(variables));
        Assert.assertEquals("/v/value", new PathSegmentExpression("var:1,var").expand(variables));
    }

    @Test
    public void testPathSegmentMatch() throws Exception {
        URITemplate template = new URITemplate("/admin{/context}");
        Map<String, String> var = new HashMap<String, String>();
        Assert.assertTrue(template.matches("/admin/foo", var));
        Assert.assertEquals("foo", var.get("context"));
        Assert.assertFalse(template.matches("/admin.do.bad", var));
        var.clear();

        template = new URITemplate("/admin{/action,sub}");
        Assert.assertTrue(template.matches("/admin/do/view", var));
        Assert.assertEquals("do", var.get("action"));
        Assert.assertEquals("view", var.get("sub"));
    }

    @Test
    public void testComaSeperatedSimpleExpressions() throws Exception {
        Map<String, String> var = new HashMap<String, String>();
        URITemplate template = new URITemplate("/admin/{one},{two},{three}*");
        Assert.assertTrue(template.matches("/admin/param1,param2,param3", var));
        Assert.assertTrue(template.matches("/admin/param1,param2,param3/test", var));
        Assert.assertTrue(template.matches("/admin/param1,param2,param3?query=param", var));
        Assert.assertEquals("param1", var.get("one"));
        Assert.assertEquals("param2", var.get("two"));
        Assert.assertEquals("param3", var.get("three"));
        var.clear();
    }

    @Test
    public void testOptionalQueryParameters() throws Exception {
        Map<String, String> var = new HashMap<String, String>();

        URITemplate template = new URITemplate("/admin/{one}");
        Assert.assertTrue(template.matches("/admin/param1?query=parameter", var));
        Assert.assertEquals("param1", var.get("one"));
        var.clear();
        Assert.assertTrue(template.matches("/admin/param1", var));
        Assert.assertEquals("param1", var.get("one"));
        var.clear();
        Assert.assertFalse(template.matches("/admin/param1/param2?query=parameter", var));

        template = new URITemplate("/admin/{one}?query={two}");
        Assert.assertTrue(template.matches("/admin/param1?query=param2", var));
        Assert.assertEquals("param1", var.get("one"));
        Assert.assertEquals("param2", var.get("two"));
        var.clear();
        Assert.assertFalse(template.matches("/admin/param1/param2?query=parameter", var));
        Assert.assertFalse(template.matches("/admin/param1", var));
    }
}
