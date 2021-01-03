Weather Measurement Project Enclosure Design

FreeCAD strategy:
	Draw each contained component and enclosure half as separate FCStd document files.
	Create an assembly3 assembly to align all.
	There is a bug that prevents drawing sketches against other documents.
	As a workaround:
		Run macro CopyPlacementFromAssembly to transfer Placement to the individual documents
		Copy each document file.
		Create another container document (AssembledComponents.FCStd) and load the copied documents.
		Create copies of each contained component (Create a copy -> Create transformed copy).  
		Drag into an enclosure half.  
		Copy the placement again.  
		Create a sub-object shape binder.
		Draw sketches against the binders and cut holes.
		Be sure to 3D print from the copy files.


The source for CopyPlacementFromAssembly:
	
for p in App.getDocument("Assembly").getObject("Parts").Group: 
    if hasattr(p, "LinkedObject"): 
        p.LinkedObject.Placement = p.Placement
        App.Console.PrintMessage(p.Placement)
        App.Console.PrintMessage("\n")