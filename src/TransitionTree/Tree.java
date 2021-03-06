/**
 * Feb 25, 2017
 * Tree.java
 * Abbas Khan
 */
package TransitionTree;

import java.util.Queue;
import java.util.Stack;

import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.eclipse.uml2.uml.Behavior;
import org.eclipse.uml2.uml.BodyOwner;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.Transition;
import org.eclipse.uml2.uml.internal.impl.FinalStateImpl;
import org.eclipse.uml2.uml.internal.impl.PseudostateImpl;
import org.eclipse.uml2.uml.internal.impl.StateImpl;

import SUT.ThreePlayerGame;
import Templates.SuiteTemplate;
import Templates.TestCaseTemplate;
/**
 * @author Abbas Khan
 *
 */
public class Tree {
	public StateNode root;
	private String CUT;
	int count=1;
	EList<StateNode> visited=new BasicEList<StateNode>();
	org.eclipse.core.internal.jobs.Queue x= new org.eclipse.core.internal.jobs.Queue();
	EList<StateNode> visited2=new BasicEList<StateNode>();
	EList<StateNode> discovered= new BasicEList<StateNode>();
	public Tree(String cls)
	{
		CUT=cls;
		//new Templates.SuiteTemplate();
		
	}
	public void initiateTree(PseudostateImpl s)
	{
		root=new StateNode(s, s.getLabel(), new BasicEList<TransitionNode>());
		EList<TransitionNode> tns=new BasicEList<TransitionNode>();
		visited.add(root);
		for(Transition t: s.getOutgoings())
		{
			boolean isGuarded=false;
			String guard=null;
			String effect=null;
			String guardBody=null;
			String effectBody=null;
			StateNode target=null;
			String name=t.getLabel();
			if(t.getGuard()!=null)
			{
				isGuarded=true;
				guard=t.getGuard().getLabel();
				for(Element g:t.getGuard().allOwnedElements())
				{
					if(g instanceof BodyOwner)
					{
						guardBody=((BodyOwner) g).getBodies().toString();
					}
				}
			}
			if(t.getEffect()!=null) // if there are actions 
			{
				effect=t.getEffect().getLabel();
				Behavior a =t.getEffect();
				if(a instanceof BodyOwner)
				{
					effectBody=((BodyOwner) a).getBodies().toString();
				}
			}
			StateImpl dest=(StateImpl) t.getTarget();
			target=new StateNode(dest, dest.getLabel(), new BasicEList<TransitionNode>());
			TransitionNode tn=new TransitionNode(isGuarded, guard, guardBody, effect, effectBody, target, name);
			tns.add(tn);
		} // for each transition ended
		root.transitions=tns;
		for(TransitionNode tn:root.transitions)
			growTheTree(tn.target);
	}
	public void growTheTree(StateNode s)
	{
		StateImpl stateObj=(StateImpl)(s.stateObj);
		if(stateObj.getOutgoings().size()==0 || isVisited(s) )//|| inTree(s)
		{
			return;
		}
		
		EList<TransitionNode> transitions=s.transitions;
		for(Transition t: stateObj.getOutgoings())
		{
			boolean isGuarded=false;
			String guard=null;
			String effect=null;
			String guardBody=null;
			String effectBody=null;
			StateNode target=null;
			String name=t.getLabel();
			if(t.getGuard()!=null)
			{
				isGuarded=true;
				guard=t.getGuard().getLabel();
				for(Element g:t.getGuard().allOwnedElements())
				{
					if(g instanceof BodyOwner)
					{
						guardBody=((BodyOwner) g).getBodies().toString();
					}
				}
			}
			if(t.getEffect()!=null) // if there are actions 
			{
				effect=t.getEffect().getLabel();
				Behavior a =t.getEffect();
				if(a instanceof BodyOwner)
				{
					effectBody=((BodyOwner) a).getBodies().toString();
				}
			}
			StateImpl dest=(StateImpl) t.getTarget();
			target=new StateNode(dest, dest.getLabel(), new BasicEList<TransitionNode>());
			TransitionNode tn=new TransitionNode(isGuarded, guard, guardBody, effect, effectBody, target, name);
			transitions.add(tn);
		} // for each transition ended
		s.transitions=transitions;
		visited.add(s);
		//visited2.add(s);
		for(TransitionNode tn:s.transitions){
			//visited2.add(tn.target);
			x.enqueue(tn.target);
		}
		while(!x.isEmpty())
		{
			growTheTree((StateNode)x.dequeue());
		}
			
	}
	public boolean isVisited(StateNode s)
	{
		for(StateNode q:visited)
		{
			if(q.name==s.name)
				return true;
		}
			
		return false;
	}
	public void printTree()
	{
		System.out.println(root.name);
		EList<TransitionNode> newts=root.transitions;
		for(TransitionNode t:root.transitions)
		{
			if(!t.isGuarded)
				System.out.println("|__"+t.name+"/"+t.effect+"--"+"-->"+t.target.name+"[further nodes:"+t.target.transitions.size()+"]");
			else
				System.out.println("|__"+t.name+"/"+t.effect+"--"+t.guardBody.replace("\n", " ")+"--> *"+t.target.name+"[further nodes:"+t.target.transitions.size()+"]");
		}
		for(TransitionNode t:newts)
			printNode(t.target);
	}
	public void printNode(StateNode s)
	{
		
		if(s.transitions.size()==0)
			return;
		System.out.println(s.name);
		for(TransitionNode t:s.transitions)
		{
			if(!t.isGuarded)
				System.out.println("|__"+t.name+"/"+t.effect+"--"+"-->"+t.target.name+"[further nodes:"+t.target.transitions.size()+"]");
			else
				System.out.println("|__"+t.name+"/"+t.effect+"--"+t.guardBody.replace("\n", " ")+"--> *"+t.target.name+"[further nodes:"+t.target.transitions.size()+"]");
		}
		for(TransitionNode t:s.transitions)
			printNode(t.target);
		
	}
	public void allTransitionsSuite()
	{
		count =0;
		TestCaseTemplate conformance= new TestCaseTemplate(CUT, "AllTransitionsTestSuite");
		conformance.body.add(CUT+" sut;"); // alpha is already made");
		conformance.body.add("@Test");
		conformance.body.add("public void testForPath"+count+"() {");
		count++;
		conformance.body.add("sut= new "+CUT+"();");
		conformance.body.add("assertEquals(\""+root.transitions.get(0).target.name+"\",sut.stateReporter());");
		growTheAllTransitionsTest(root.transitions.get(0).target, conformance);
		conformance.body.add("}");
		conformance.generateTemplateFile();
	}
	public void growTheAllTransitionsTest(StateNode s, TestCaseTemplate tc)
	{
		discovered.add(s);
		if(s.transitions.size()==0)
		{
			for(int i=0; i<discovered.size();i++)
			{
				StateNode t= discovered.get(i);
				if(i==discovered.size()-1)
				{
					//tc.body.add("assertEquals(\""+t.name+"\", sut.stateReporter()); ");
				}
				else
				{
					
					for(TransitionNode x:t.transitions)
					{
						String e="";
						StateNode ahead=discovered.get(i+1);
						if(ahead.name==x.target.name)
						{
							e=t.name;
							if(x.isGuarded)
							{
								tc.body.add("/* Please DIY satisfy the guard "+x.guard+" with body:"+ x.guardBody+"*/");
							}
							tc.body.add("sut."+x.name+"; ");
							tc.body.add("assertEquals(\""+x.target.name+"\", sut.stateReporter()); ");
						}
							
					}
				}
				
				
				//discovered.remove(discovered.size()-1);
			}
			discovered.remove(discovered.size()-1);
				//System.out.println("----------------");
		}
		for(TransitionNode t:s.transitions)
		{
			growTheAllTransitionsTest(t.target, tc);
			tc.body.add("}");
			tc.body.add("@Test");
			tc.body.add("public void testForPath"+count+"() {");
			tc.body.add("sut=new "+CUT+"();");
			count++;
			//discovered.remove(discovered.size()-1);
		}
	}
	public void allRoundTripPathsSuite()
	{
		count=1;
		//StateNode r=root;
		visited=null;
		visited= new BasicEList<StateNode>();
		TestCaseTemplate tc1= new TestCaseTemplate(CUT, "AllRoundTripPathsSuite");
		tc1.body.add("ThreePlayerGame sut= new ThreePlayerGame(); // alpha is already made");
		tc1.body.add("@Test");
		tc1.body.add("public void testForRoundTripPath"+count+"() {");
		count++;
		if(root.transitions.size()==1)
		{
			TransitionNode temp=root.transitions.get(0);
			//tc1.body.add("assertEquals(\""+temp.target.name+"\", "+"sut.stateReporter());");
			//nodesStack.push(temp);
			growTheRoundTripTest(temp.target, tc1);
		}
		else{
			for(TransitionNode t:root.transitions)
			{
				
				tc1.body.add("sut."+t.effect+";");
				
				//tc1.body.add("assertEquals(\""+t.target.name+"\", "+"sut.stateReporter());");
				growTheRoundTripTest(t.target, tc1);
			}
		}
		tc1.body.add("}");
		tc1.generateTemplateFile();
	}
	public void growTheRoundTripTest(StateNode s, TestCaseTemplate tc)
	{
		if(s.transitions.size()==0)
		{
			return;
		}
		if(isVisited(s))
		{
			tc.body.add("assertEquals(\""+s.name+"\", "+"sut.stateReporter());");;
			tc.body.add("}");
			tc.body.add("@Test");
			tc.body.add("public void testForPath"+count+"() {");
			count++;
			return;
		}
		
		for(TransitionNode t:s.transitions)
		{
			if(!t.isGuarded)
			{
				tc.body.add("sut."+t.name+";");
				//tc.body.add("assertEquals(\""+t.target.name+"\", "+"sut.stateReporter());");
				growTheRoundTripTest(t.target, tc);
			}
			else
			{
				tc.body.add("/* for Guard false */");
				tc.body.add("sut."+t.name+";");
				//tc.body.add("assertEquals(\""+s.name+"\", "+"sut.stateReporter()); // should be in same state");
				tc.body.add("/* for Guard True please DIY, Satisfy the guard '"+t.guard+"' with body: "+t.guardBody +"*/");
				growTheRoundTripTest(t.target, tc);
			}
			visited.add(t.target);
		}
	}
}
